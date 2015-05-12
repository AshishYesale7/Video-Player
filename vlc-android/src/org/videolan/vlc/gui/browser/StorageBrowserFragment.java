/*
 * *************************************************************************
 *  StorageBrowserFragment.java
 * **************************************************************************
 *  Copyright © 2015 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *  ***************************************************************************
 */

package org.videolan.vlc.gui.browser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import org.videolan.libvlc.Media;
import org.videolan.vlc.MediaWrapper;
import org.videolan.vlc.R;
import org.videolan.vlc.util.AndroidDevices;

import java.util.ArrayList;

public class StorageBrowserFragment extends FileBrowserFragment {

    public static final String KEY_IN_MEDIALIB = "key_in_medialib";

    boolean mScannedDirectory = false;

    public StorageBrowserFragment(){
        mHandler = new BrowserFragmentHandler(this);
        mAdapter = new StorageBrowserAdapter(this);
        ROOT = AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY;
    }

    @Override
    protected Fragment createFragment() {
        return new StorageBrowserFragment();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null)
            bundle = getArguments();
        if (bundle != null){
            mScannedDirectory = bundle.getBoolean(KEY_IN_MEDIALIB);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_MEDIALIB, mScannedDirectory);
    }

    @Override
    protected void browseRoot() {
        String storages[] = AndroidDevices.getMediaDirectories();
        BaseBrowserAdapter.Storage storage;
        for (String mediaDirLocation : storages) {
            storage = new BaseBrowserAdapter.Storage(mediaDirLocation);
            if (TextUtils.equals(AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY, mediaDirLocation))
                storage.setName(getString(R.string.internal_memory));
            mAdapter.addItem(storage, false, false);
        }
        mHandler.sendEmptyMessage(BrowserFragmentHandler.MSG_HIDE_LOADING);
        if (mReadyToDisplay) {
            updateEmptyView();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void update() {
        mAdapter.updateMediaDirs();
        super.update();
    }

    @Override
    public void onMediaAdded(int index, Media media) {
        if (media.getType() != Media.Type.Directory)
            return;
        super.onMediaAdded(index, media);
    }

    protected void updateDisplay() {
        if (!mAdapter.isEmpty()) {
            if (mSavedPosition > 0) {
                mLayoutManager.scrollToPositionWithOffset(mSavedPosition, 0);
                mSavedPosition = 0;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void browse (MediaWrapper media, int position, boolean scanned){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment next = createFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_MEDIA, media);
        args.putBoolean(KEY_IN_MEDIALIB, mScannedDirectory || scanned);
        next.setArguments(args);
        ft.replace(R.id.fragment_placeholder, next, media.getLocation());
        ft.addToBackStack(mMrl);
        ft.commit();
    }
}
