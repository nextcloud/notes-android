package it.niedermann.owncloud.notes.share.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.databinding.ItemAddPublicShareBinding;
import it.niedermann.owncloud.notes.databinding.ItemInternalShareLinkBinding;
import it.niedermann.owncloud.notes.databinding.ItemShareLinkShareBinding;
import it.niedermann.owncloud.notes.databinding.ItemShareShareBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.adapter.holder.InternalShareViewHolder;
import it.niedermann.owncloud.notes.share.adapter.holder.LinkShareViewHolder;
import it.niedermann.owncloud.notes.share.adapter.holder.NewLinkShareViewHolder;
import it.niedermann.owncloud.notes.share.adapter.holder.ShareViewHolder;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;

/**
 * Adapter to show a user/group/email/remote in Sharing list in file details view.
 */
public class ShareeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Account account;
    private final ShareeListAdapterListener listener;
    private final Activity activity;
    private List<OCShare> shares;

    public ShareeListAdapter(Activity activity,
                             List<OCShare> shares,
                             ShareeListAdapterListener listener,
                             Account account) {
        this.activity = activity;
        this.shares = shares;
        this.listener = listener;
        this.account = account;

        sortShares();
    }

    @Override
    public int getItemViewType(int position) {
        if (shares == null) {
            return 0;
        }

        if (position < 0 || position >= shares.size()) {
            return 0;
        }

        final var share = shares.get(position);
        if (share == null) {
            return 0;
        }

        final var shareType = share.getShareType();
        if (shareType == null) {
            return 0;
        }

        return shareType.getValue();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (ShareType.fromValue(viewType)) {
            case PUBLIC_LINK, EMAIL -> {
                return new LinkShareViewHolder(
                        ItemShareLinkShareBinding.inflate(LayoutInflater.from(activity),
                                parent,
                                false),
                        activity);
            }
             case NEW_PUBLIC_LINK -> {
                return new NewLinkShareViewHolder(
                        ItemAddPublicShareBinding.inflate(LayoutInflater.from(activity),
                                parent,
                                false)
                );
            }
            case INTERNAL -> {
                return new InternalShareViewHolder(
                        ItemInternalShareLinkBinding.inflate(LayoutInflater.from(activity), parent, false),
                        activity);
            }
            default -> {
                return new ShareViewHolder(ItemShareShareBinding.inflate(LayoutInflater.from(activity),
                        parent,
                        false),
                        account,
                        activity);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (shares == null || shares.size() <= position) {
            return;
        }

        final OCShare share = shares.get(position);


        if (holder instanceof LinkShareViewHolder publicShareViewHolder) {
            publicShareViewHolder.bind(share, listener);
        } else if (holder instanceof InternalShareViewHolder internalShareViewHolder) {
            internalShareViewHolder.bind(share, listener);
        } else if (holder instanceof NewLinkShareViewHolder newLinkShareViewHolder) {
            newLinkShareViewHolder.bind(listener);
        } else {
            ShareViewHolder userViewHolder = (ShareViewHolder) holder;
            userViewHolder.bind(share, listener);
        }
    }

    @Override
    public long getItemId(int position) {
        return shares.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return shares.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addShares(List<OCShare> sharesToAdd) {
        shares.addAll(sharesToAdd);
        sortShares();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void remove(OCShare share) {
        shares.remove(share);
        notifyDataSetChanged();
    }

    /**
     * sort all by creation time, then email/link shares on top
     */
    protected final void sortShares() {
        List<OCShare> links = new ArrayList<>();
        List<OCShare> users = new ArrayList<>();

        for (OCShare share : shares) {
            if (share.getShareType()  != null) {
                if (ShareType.PUBLIC_LINK == share.getShareType() || ShareType.EMAIL == share.getShareType()) {
                    links.add(share);
                } else if (share.getShareType() != ShareType.INTERNAL) {
                    users.add(share);
                }
            }
        }

        links.sort((o1, o2) -> Long.compare(o2.getSharedDate(), o1.getSharedDate()));
        users.sort((o1, o2) -> Long.compare(o2.getSharedDate(), o1.getSharedDate()));

        shares = links;
        shares.addAll(users);

        final OCShare ocShare = new OCShare();
        ocShare.setShareType(ShareType.INTERNAL);
        shares.add(ocShare);
    }

    public List<OCShare> getShares() {
        return shares;
    }

    public void removeNewPublicShare() {
        for (OCShare share : shares) {
            if (share.getShareType() == ShareType.NEW_PUBLIC_LINK) {
                shares.remove(share);
                break;
            }
        }
    }
}
