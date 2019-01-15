package eastwind.ramd.server;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.ramd.model.Vote;
import eastwind.ramd.ramd.DataService;
import eastwind.ramd.support.DelayedExecutor;
import eastwind.ramd.support.StateFul;

public class ElectEngine extends StateFul<ElectState> {

	private static Logger LOGGER = LoggerFactory.getLogger(ElectEngine.class);

	private static final int TIMEOUT = 50;
	private static final int TASK_DELAY = 100;

	private RamdGroup ramdGroup;
	private DataService dataService;
	private Server votedFor;
	// initial pre-vote pre-candidate candidate leader
	private int step;
	private int modCount;
	private Map<RamdServer, Server> votedForOfOthers = new HashMap<>();
	private Set<RamdServer> notifyed = new HashSet<>();
	private DelayedExecutor delayedExecutor;

	public ElectEngine(RamdGroup ramdGroup, DelayedExecutor delayedExecutor, DataService dataService) {
		super.state = ElectState.INITIAL;
		this.ramdGroup = ramdGroup;
		this.delayedExecutor = delayedExecutor;
		this.dataService = dataService;
	}

	public void tryElect(RamdServer newServer) {
		// electing
		if (step > 0) {
			if (newServer.isOnline() && votedFor != null && !notifyed.contains(newServer)) {
				if (votedFor == getMyself()) {
					Vote forVote = Vote.forVote(dataService.getCurrentTerm(), getMyself().getLogId());
					doForVoteOne(forVote, newServer);
				} else {
					Vote notify = Vote.vote(votedFor.addressStr);
					doNotifyOne(notify, newServer);
				}
			}
		} else if (ramdGroup.isAllOnOffLine()) {
			// TODO check term and multi leaders
			Server leader = ramdGroup.findLeader();
			if (leader == null || leader.isOffline2()) {
				doPreVote();
			}
		}
	}

	public void onElected(Consumer<Void> consumer) {
		super.onState(ElectState.ELECTED, consumer);
	}

	private void doPreVote() {
		if (votedFor != null) {
			return;
		}
		LOGGER.info("do pre-vote..., try {} times.", modCount);
		this.step = 1;
		long currentTerm = dataService.getCurrentTerm();
		long logId = dataService.getLogId();
		Vote preVote = Vote.preVote(currentTerm, logId);
		GroupExchangeConsumer gec = ramdGroup.exchange(preVote, 50);
		gec.allOf(t -> {
			if (step == 1) {
				modCount++;
				delayedExecutor.delayExecute(TASK_DELAY, de -> doPreVote());
			}
		});
		gec.anyOf(t -> {
			if (gec.isHalfCompleted()) {
				Stream<ExchangeContext> stream = gec.getCompleted().stream();
				int n = (int) stream.filter(ec -> ((Vote) ec.getResult()).type == Vote.AGREE).count();
				if (ramdGroup.isGtThenHalf(n + 1)) {
					doForVote();
					gec.allOf(null).anyOf(null);
				} else {
					stream = gec.getCompleted().stream();
					n = (int) stream.filter(ec -> ((Vote) ec.getResult()).type == Vote.OPPOSE).count();
					if (ramdGroup.isGtThenHalf(n)) {
						// TODO
					}
				}
			}
		});
	}

	private void doForVote() {
		this.step = 2;
		delayedExecutor.delayExecute(defaultVoteDelay(), de -> {
			if (votedFor == null) {
				votedFor = getMyself();
				LOGGER.info("vote to {}.", getMyself());
				this.step = 3;
				long term = dataService.incrementTerm();
				long logId = dataService.getLogId();
				Vote forVote = Vote.forVote(term, logId);
				for (RamdServer server : ramdGroup.getAll()) {
					if (server.isOnline()) {
						doForVoteOne(forVote, server);
					}
				}
				delayedExecutor.delayExecute(TASK_DELAY >> 1, new CandidateTask());
			}
		});
	}

	private void doForVoteOne(Vote forVote, RamdServer server) {
		ExchangeContext ec = server.exchange(forVote, TIMEOUT);
		ec.onSuccess(t -> {
			notifyed.add(server);
			Vote back = (Vote) t;
			if (back.type == Vote.AGREE) {
				// do nothing
			}
		});
	}

	private void doNotify() {
		Vote vote = Vote.vote(votedFor.addressStr);
		for (RamdServer server : ramdGroup.getAll()) {
			if (server.isOnline()) {
				doNotifyOne(vote, server);
			}
		}
	}

	private void doNotifyOne(Vote vote, RamdServer server) {
		server.exchange(vote, TIMEOUT).onSuccess(t -> {
			notifyed.add(server);
		});
	}

	public Vote recvVote(RamdServer from, Vote inbound) {
		if (inbound.type == Vote.PRE_VOTE) {
			return recvPreVote(inbound);
		} else if (inbound.type == Vote.FOR_VOTE) {
			return recvForVote(from, inbound);
		} else if (inbound.type == Vote.VOTE) {
			Server to = ramdGroup.get(inbound.target);
			votedForOfOthers.put(from, to);
			return Vote.agree();
		} else if (inbound.type == Vote.LEADER) {
			getMyself().setRole(Role.FOLLOWER);

			from.setRole(Role.LEADER);
			from.setTerm(inbound.term);
			from.setLogId(inbound.logId);

			LOGGER.info("new leader: {}.", from);
			changeState(ElectState.ELECTED, null);
		}
		return null;
	}

	private Vote recvForVote(RamdServer from, Vote inbound) {
		votedForOfOthers.put(from, from);
		if (votedFor == null && getMyself().getCurrentTerm() < inbound.term
				&& getMyself().getLogId() <= inbound.logId) {
			this.votedFor = from;
			dataService.setCurrentTerm(inbound.term);
			LOGGER.info("vote to {}.", from);
			step = 2;
			doNotify();
			return Vote.agree();
		} else {
			return Vote.oppose();
		}
	}

	private Vote recvPreVote(Vote inbound) {
		Server server = ramdGroup.findLeader();
		if (server == null) {
			if (ramdGroup.isAllOnOffLine()) {
				if (inbound.term >= getMyself().getCurrentTerm() && inbound.logId >= getMyself().getLogId()) {
					return Vote.agree();
				} else {
					return Vote.oppose();
				}
			} else {
				return Vote.pending();
			}
		} else {
			if (server.isOffline2()) {
				if (inbound.term >= getMyself().getCurrentTerm() && inbound.logId >= getMyself().getLogId()) {
					return Vote.agree();
				} else {
					return Vote.oppose();
				}
			} else {
				return Vote.oppose();
			}
		}
	}

	private Map<Server, Integer> countVotes() {
		Map<Server, Integer> countOfVotes = new HashMap<>();
		for (Server server : votedForOfOthers.values()) {
			countOfVotes.compute(server, (k, v) -> v == null ? 1 : v + 1);
		}
		if (votedFor != null) {
			countOfVotes.compute(votedFor, (k, v) -> v == null ? 1 : v + 1);
		}
		return countOfVotes;
	}

	private Server getMyself() {
		return ramdGroup.getMyself();
	}

	private long defaultVoteDelay() {
		return RandomUtils.nextInt(50, 250);
	}

	private void reset() {
		step = 0;
		notifyed.clear();
		votedForOfOthers.clear();
	}

	private class CandidateTask implements Consumer<DelayedExecutor> {

		private int uselessRun;
		private int _modCount;

		@Override
		public void accept(DelayedExecutor de) {
			if (step == 0) {
				return;
			}
			if (ramdGroup.isEqAll(votedForOfOthers.size() + 1) || uselessRun > 2) {
				if (ramdGroup.isGtThenHalf(votedForOfOthers.size() + 1)) {
					Map<Server, Integer> voteCounts = countVotes();
					int mine = voteCounts.get(getMyself());
					if (ramdGroup.isGtThenHalf(mine)) {
						LOGGER.info("new leader: {}, votes:{}/{}", getMyself(), mine, ramdGroup.getSize());
						reset();
						getMyself().setRole(Role.LEADER);
						Vote vote = Vote.leader(dataService.getCurrentTerm(), dataService.getLogId());
						for (RamdServer server : ramdGroup.getAll()) {
							server.send(vote);
							changeState(ElectState.ELECTED, null);
						}
					} else {
						int max = voteCounts.entrySet().stream().max(Comparator.comparingInt(en -> en.getValue())).get()
								.getValue();
						if (mine < max) {
							// withdraw
							return;
						} else {
							// re-vote
							modCount = 0;
							notifyed.clear();
							votedForOfOthers.clear();
							de.delayExecute(defaultVoteDelay(), t -> doForVote());
						}
					}
				}
			} else {
				if (_modCount == modCount) {
					uselessRun++;
				} else {
					uselessRun = 0;
					_modCount = modCount;
				}
				de.delayExecute(TASK_DELAY, this);
			}
		}

	}
}
