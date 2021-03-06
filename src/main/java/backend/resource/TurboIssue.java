package backend.resource;

import backend.IssueMetadata;
import backend.resource.serialization.SerializableIssue;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;
import prefs.Preferences;
import util.HTLog;
import util.Utility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static util.Utility.replaceNull;

/**
 * The guidelines in this class apply to all TurboResources.
 */
@SuppressWarnings("unused")
public class TurboIssue {
    private static final Logger logger = HTLog.get(TurboIssue.class);

    public static final String STATE_CLOSED = "closed";
    public static final String STATE_OPEN = "open";

    /**
     * Must have reasonable, NON-NULL defaults.
     * Should be primitive types, or at least easily-serializable ones.
     * Should be specified in order.
     * Should be immutable as much as possible.
     */
    private void ______SERIALIZED_FIELDS______() {
    }

    // Immutable
    private final int id;
    private final String creator;
    private final LocalDateTime createdAt;
    private final boolean isPullRequest;


    // Mutable
    private String title;
    private String description;
    private LocalDateTime updatedAt;
    private int commentCount;
    private boolean isOpen;
    private Optional<String> assignee;
    private List<String> labels;
    private Optional<Integer> milestone;

    /**
     * Metadata associated with issues that is not serialized.
     */
    private void ______TRANSIENT_FIELDS______() {
    }

    private final String repoId;
    private IssueMetadata metadata;
    private Optional<LocalDateTime> markedReadAt;

    private void ______CONSTRUCTORS______() {
    }

    /**
     * Default constructor: provides reasonable defaults for things.
     */
    public TurboIssue(String repoId, int id, String title) {
        this.id = id;
        this.creator = "";
        this.createdAt = LocalDateTime.now();
        this.isPullRequest = false;

        mutableFieldDefaults();

        this.title = title;
        this.repoId = repoId;
    }

    /**
     * Immutable-conscious constructor.
     */
    public TurboIssue(String repoId, int id, String title,
                      String creator, LocalDateTime createdAt, boolean isPullRequest) {
        this.id = id;
        this.creator = creator;
        this.createdAt = createdAt;
        this.isPullRequest = isPullRequest;

        mutableFieldDefaults();

        this.title = title;
        this.repoId = repoId;
    }

    // Copy constructor
    public TurboIssue(TurboIssue issue) {
        this.id = issue.id;
        this.title = issue.title;
        this.creator = issue.creator;
        this.createdAt = issue.createdAt;
        this.isPullRequest = issue.isPullRequest;

        this.description = issue.description;
        this.updatedAt = replaceNull(issue.updatedAt, this.createdAt);
        this.commentCount = issue.commentCount;
        this.isOpen = issue.isOpen;
        this.assignee = issue.assignee;
        this.labels = new ArrayList<>(issue.labels);
        this.milestone = issue.milestone;

        this.metadata = issue.metadata;
        this.repoId = issue.repoId;
        this.markedReadAt = issue.markedReadAt;
    }

    public TurboIssue(String repoId, Issue issue) {
        this.id = issue.getNumber();
        this.title = issue.getTitle() == null
            ? ""
            : issue.getTitle();
        this.creator = issue.getUser().getLogin();
        this.createdAt = Utility.dateToLocalDateTime(issue.getCreatedAt());
        this.isPullRequest = isPullRequest(issue);

        this.description = issue.getBody() == null
            ? ""
            : issue.getBody();
        this.updatedAt = issue.getUpdatedAt() != null ?
                Utility.dateToLocalDateTime(issue.getUpdatedAt()) : this.createdAt;
        this.commentCount = issue.getComments();
        this.isOpen = issue.getState().equals(STATE_OPEN);
        this.assignee = issue.getAssignee() == null
            ? Optional.empty()
            : Optional.of(issue.getAssignee().getLogin());
        this.labels = issue.getLabels().stream()
            .map(Label::getName)
            .collect(Collectors.toList());
        this.milestone = issue.getMilestone() == null
            ? Optional.empty()
            : Optional.of(issue.getMilestone().getNumber());

        this.metadata = IssueMetadata.empty();
        this.repoId = repoId;
        this.markedReadAt = Optional.empty();
    }

    public TurboIssue(String repoId, SerializableIssue issue) {
        this.id = issue.getId();
        this.creator = issue.getCreator();
        this.createdAt = issue.getCreatedAt();
        this.isPullRequest = issue.isPullRequest();

        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.updatedAt = replaceNull(issue.getUpdatedAt(), this.createdAt);
        this.commentCount = issue.getCommentCount();
        this.isOpen = issue.isOpen();
        this.assignee = issue.getAssignee();
        this.labels = issue.getLabels();
        this.milestone = issue.getMilestone();

        this.metadata = IssueMetadata.empty();
        this.repoId = repoId;
        this.markedReadAt = Optional.empty();
    }

    private void ______CONSTRUCTOR_HELPER_FUNCTIONS______() {
    }

    private static boolean isPullRequest(Issue issue) {
        return issue.getPullRequest() != null && issue.getPullRequest().getUrl() != null;
    }

    private void mutableFieldDefaults() {
        this.title = "";
        this.description = "";
        this.updatedAt = replaceNull(this.createdAt, LocalDateTime.now());
        this.commentCount = 0;
        this.isOpen = true;
        this.assignee = Optional.empty();
        this.labels = new ArrayList<>();
        this.milestone = Optional.empty();

        this.metadata = IssueMetadata.empty();
        this.markedReadAt = Optional.empty();
    }

    /**
     * Transient state may not always be transferred between issues. For example,
     * when an issue is updated, the new issue is constructed from an external object,
     * not an existing issue, so transient state on the old one is lost if the old issue
     * is just replaced by the new. This method is required in such cases.
     * @param fromIssue
     */
    private void transferTransientState(TurboIssue fromIssue) {
        this.metadata = fromIssue.metadata.invalidate();
        this.markedReadAt = fromIssue.markedReadAt;
    }

    /**
     * Conceptually, operations on issues. They should only modify non-serialized fields.
     */
    private void ______METHODS______() {
    }

    @Override
    public String toString() {
        return repoId + " #" + id;
    }

    /**
     * Takes lists of TurboIssues and reconciles the changes between them,
     * returning a list of TurboIssues with updates from the second.
     */
    public static List<TurboIssue> reconcile(String repoId, List<TurboIssue> existing, List<TurboIssue> changed) {
        existing = new ArrayList<>(existing);
        for (TurboIssue issue : changed) {
            int id = issue.getId();

            // TODO O(n^2), fix by preprocessing and copying into a map
            Optional<Integer> corresponding = findIssueWithId(existing, id);
            if (corresponding.isPresent()) {

                // issue is constructed from an external Issue object.
                // It won't have the transient state that its TurboIssue
                // counterpart has, so we have to explicitly transfer it.
                TurboIssue newIssue = new TurboIssue(issue);
                newIssue.transferTransientState(existing.get(corresponding.get()));

                existing.set(corresponding.get(), newIssue);
            } else {
                existing.add(new TurboIssue(issue));
            }
        }
        return existing;
    }

    /**
     * Updates data for issues with corresponding pull requests. Original list of
     * issues and original issue instances are not mutated
     * @param issues
     * @param pullRequests
     * @return a new list of issues
     */
    public static List<TurboIssue> combineWithPullRequests(List<TurboIssue> issues,
                                                           List<PullRequest> pullRequests) {
        issues = new ArrayList<>(issues);

        for (PullRequest pullRequest : pullRequests) {
            int id = pullRequest.getNumber();

            Optional<Integer> corresponding = findIssueWithId(issues, id);
            if (corresponding.isPresent()) {
                TurboIssue issue = issues.get(corresponding.get());
                issues.set(corresponding.get(), issue.combineWithPullRequest(pullRequest));
            } else {
                String errorMsg = "No corresponding issue for pull request " + pullRequest;
                logger.error(errorMsg);
            }
        }

        return issues;
    }

    /**
     * Combines data from a corresponding pull request with data in this issue
     * This method returns a new combined issue and does not mutate this issue
     * @param pullRequest
     * @return new new combined issue
     */
    public TurboIssue combineWithPullRequest(PullRequest pullRequest) {
        TurboIssue newIssue = new TurboIssue(this);

        if (pullRequest.getUpdatedAt() == null) {
            return newIssue;
        }

        LocalDateTime pullRequestUpdatedAt = Utility.dateToLocalDateTime(pullRequest.getUpdatedAt());
        if (pullRequestUpdatedAt.isBefore(newIssue.getUpdatedAt())) {
            return newIssue;
        }

        newIssue.setUpdatedAt(pullRequestUpdatedAt);
        return newIssue;
    }

    public static Optional<Integer> findIssueWithId(List<TurboIssue> issues, int id) {
        int i = 0;
        for (TurboIssue issue : issues) {
            if (issue.getId() == id) {
                return Optional.of(i);
            }
            ++i;
        }
        return Optional.empty();
    }

    private void ______BOILERPLATE______() {
    }

    public String getRepoId() {
        return repoId;
    }
    public int getId() {
        return id;
    }
    public String getCreator() {
        return creator;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public boolean isPullRequest() {
        return isPullRequest;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = replaceNull(updatedAt, this.createdAt);
    }
    public int getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    public boolean isOpen() {
        return isOpen;
    }
    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
    public Optional<String> getAssignee() {
        return assignee;
    }
    public void setAssignee(String assignee) {
        this.assignee = Optional.of(assignee);
    }
    public void setAssignee(TurboUser assignee) {
        setAssignee(assignee.getLoginName());
    }
    public List<String> getLabels() {
        return labels;
    }
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
    public void addLabel(String label) {
        this.labels.add(label);
    }
    public void addLabel(TurboLabel label) {
        addLabel(label.getActualName());
    }
    public Optional<Integer> getMilestone() {
        return milestone;
    }
    public void setMilestone(Integer milestone) {
        this.milestone = Optional.of(milestone);
    }
    public void setMilestone(TurboMilestone milestone) {
        setMilestone(milestone.getId());
    }
    public IssueMetadata getMetadata() {
        return metadata;
    }
    public void setMetadata(IssueMetadata metadata) {
        this.metadata = metadata;
    }

    public Optional<LocalDateTime> getMarkedReadAt() {
        return markedReadAt;
    }

    public void setMarkedReadAt(Optional<LocalDateTime> markedReadAt) {
        this.markedReadAt = markedReadAt;
    }

    public boolean isCurrentlyRead() {
        if (!getMarkedReadAt().isPresent()) {
            return false;
        }

        return getMarkedReadAt().get().isAfter(getUpdatedAt());
    }

    public void markAsRead(Preferences prefs) {
        LocalDateTime now = LocalDateTime.now();
        setMarkedReadAt(Optional.of(now));
        prefs.setMarkedReadAt(getRepoId(), getId(), getMarkedReadAt().get());
    }

    public void markAsUnread(Preferences prefs) {
        setMarkedReadAt(Optional.empty());
        prefs.clearMarkedReadAt(getRepoId(), getId());
    }

    /**
     * Metadata is not considered for equality.
     * In general only serialised fields are.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TurboIssue issue = (TurboIssue) o;
        return commentCount == issue.commentCount &&
                id == issue.id && isOpen == issue.isOpen &&
                isPullRequest == issue.isPullRequest &&
                !(assignee != null ? !assignee.equals(issue.assignee) : issue.assignee != null) &&
                !(createdAt != null ? !createdAt.equals(issue.createdAt) : issue.createdAt != null) &&
                !(creator != null ? !creator.equals(issue.creator) : issue.creator != null) &&
                !(description != null ? !description.equals(issue.description) : issue.description != null) &&
                !(labels != null ? !labels.equals(issue.labels) : issue.labels != null) &&
                !(milestone != null ? !milestone.equals(issue.milestone) : issue.milestone != null) &&
                !(title != null ? !title.equals(issue.title) : issue.title != null) &&
                !(updatedAt != null ? !updatedAt.equals(issue.updatedAt) : issue.updatedAt != null) &&
                !(markedReadAt != null ? !markedReadAt.equals(issue.markedReadAt) : issue.markedReadAt != null);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (creator != null ? creator.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (isPullRequest ? 1 : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (markedReadAt != null ? markedReadAt.hashCode() : 0);
        result = 31 * result + commentCount;
        result = 31 * result + (isOpen ? 1 : 0);
        result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (milestone != null ? milestone.hashCode() : 0);
        return result;
    }
}
