/*
 * Copyright (C) 2011 Sony Ericsson Mobile Communications AB
 * Copyright (C) 2012 Sony Mobile Communications AB
 *
 * This file is part of ChkBugReport.
 *
 * ChkBugReport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * ChkBugReport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ChkBugReport.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sonyericsson.chkbugreport.plugins.stacktrace;

import com.sonyericsson.chkbugreport.doc.Anchor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/* package */ class StackTrace implements Iterable<StackTraceItem> {

    private String mName;
    private Vector<StackTraceItem> mStack = new Vector<StackTraceItem>();
    private int mTid;
    private int mPrio;
    private String mState;
    private int mWaitOn;
    private Process mProc;
    private HashMap<String, String> mProps = new HashMap<String, String>();
    private int mPid;
    private StackTrace mAidlDep;
    private Anchor mAnchor;

    public StackTrace(Process process, String name, int tid, int prio, String threadState) {
        mProc = process;
        mName = name;
        mTid = tid;
        mPrio = prio;
        mState = threadState;
        mWaitOn = -1;
        mAnchor = new Anchor("tid_" + tid);
    }

    public void parseProperties(String s) {
        String kvs[] = s.split(" ");
        for (String kv : kvs) {
            if (kv.length() == 0) continue;
            String pair[] = kv.split("=");
            if (pair.length != 2) continue;
            mProps.put(pair[0], pair[1]);

            // Handle some properties specially
            if (pair[0].equals("sysTid")) {
                try {
                    mPid = Integer.parseInt(pair[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getProperty(String key) {
        return mProps.get(key);
    }

    public void setStyle(int from, int to, String style) {
        from = Math.max(0, from);
        to = Math.min(getCount(), to);
        for (int i = from; i < to; i++) {
            get(i).setStyle(style);
        }
    }

    public int findMethod(String methodName) {
        int cnt = getCount();
        for (int i = 0; i < cnt; i++) {
            if (get(i).getMethod().equals(methodName)) {
                return i;
            }
        }
        return -1;
    }

    public Process getProcess() {
        return mProc;
    }

    public String getName() {
        return mName;
    }

    public int getTid() {
        return mTid;
    }

    public int getPid() {
        return mPid;
    }

    public int getPrio() {
        return mPrio;
    }

    public int getWaitOn() {
        return mWaitOn;
    }

    public void setWaitOn(int tid) {
        mWaitOn = tid;
    }

    public String getState() {
        return mState;
    }

    public void addStackTraceItem(StackTraceItem item) {
        mStack.add(item);
    }

    public int getCount() {
        return mStack.size();
    }

    public StackTraceItem get(int idx) {
        return mStack.get(idx);
    }

    public void setAidlDependency(StackTrace dstThread) {
        mAidlDep = dstThread;
    }

    public StackTrace getAidlDependency() {
        return mAidlDep;
    }

    public StackTrace getDependency() {
        if (mWaitOn >= 0) {
            return getProcess().findTid(mWaitOn);
        }
        return mAidlDep;
    }

    @Override
    public Iterator<StackTraceItem> iterator() {
        return mStack.iterator();
    }

    public Anchor getAnchor() {
        return mAnchor;
    }

}

