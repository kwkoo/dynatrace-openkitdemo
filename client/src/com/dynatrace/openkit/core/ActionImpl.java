/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;

import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Actual implementation of the {@link Action} interface.
 */
public class ActionImpl implements Action {

    private static final WebRequestTracer NULL_WEB_REQUEST_TRACER = new NullWebRequestTracer();

    // Action ID, name and parent ID (default: null)
    private final int id;
    private final String name;
    private ActionImpl parentAction = null;

    // start/end time & sequence number
    private final long startTime;
    private final AtomicLong endTime = new AtomicLong(-1);
    private final int startSequenceNo;
    private int endSequenceNo = -1;

    // Beacon reference
    private final Beacon beacon;

    private SynchronizedQueue<Action> thisLevelActions = null;

    // *** constructors ***

    ActionImpl(Beacon beacon, String name, SynchronizedQueue<Action> thisLevelActions) {
        this(beacon, name, null, thisLevelActions);
    }

    ActionImpl(Beacon beacon, String name, ActionImpl parentAction, SynchronizedQueue<Action> thisLevelActions) {
        this.beacon = beacon;
        this.parentAction = parentAction;

        this.startTime = beacon.getCurrentTimestamp();
        this.startSequenceNo = beacon.createSequenceNumber();
        this.id = beacon.createID();
        this.name = name;

        this.thisLevelActions = thisLevelActions;
        this.thisLevelActions.put(this);
    }

    // *** Action interface methods ***

    @Override
    public Action reportEvent(String eventName) {
        if (!isActionLeft()) {
            beacon.reportEvent(this, eventName);
        }
        return this;
    }

    @Override
    public Action reportValue(String valueName, int value) {
        if (!isActionLeft()) {
            beacon.reportValue(this, valueName, value);
        }
        return this;
    }

    @Override
    public Action reportValue(String valueName, double value) {
        if (!isActionLeft()) {
            beacon.reportValue(this, valueName, value);
        }
        return this;
    }

    @Override
    public Action reportValue(String valueName, String value) {
        if (!isActionLeft()) {
            beacon.reportValue(this, valueName, value);
        }
        return this;
    }

    @Override
    public Action reportError(String errorName, int errorCode, String reason) {
        if (!isActionLeft()) {
            beacon.reportError(this, errorName, errorCode, reason);
        }
        return this;
    }

    @Override
    public WebRequestTracer traceWebRequest(URLConnection connection) {
        if (!isActionLeft()) {
            return new WebRequestTracerURLConnection(beacon, this, connection);
        }

        return NULL_WEB_REQUEST_TRACER;
    }

    @Override
    public WebRequestTracer traceWebRequest(String url) {
        if (!isActionLeft()) {
            return new WebRequestTracerStringURL(beacon, this, url);
        }

        return NULL_WEB_REQUEST_TRACER;
    }

    @Override
    public Action leaveAction() {
        // check if leaveAction() was already called before by looking at endTime
        if (!endTime.compareAndSet(-1, beacon.getCurrentTimestamp())) {
            return parentAction;
        }

        return doLeaveAction();
    }

    protected Action doLeaveAction() {
        // set end time and end sequence number
        endTime.set(beacon.getCurrentTimestamp());
        endSequenceNo = beacon.createSequenceNumber();

        // add Action to Beacon
        beacon.addAction(this);

        // remove Action from the Actions on this level
        thisLevelActions.remove(this);

        return parentAction;            // can be null if there's no parent Action!
    }

    // *** getter methods ***

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getParentID() {
        return parentAction == null ? 0 : parentAction.getID();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime.get();
    }

    public int getStartSequenceNo() {
        return startSequenceNo;
    }

    public int getEndSequenceNo() {
        return endSequenceNo;
    }

    boolean isActionLeft() {
        return getEndTime() != -1;
    }

}
