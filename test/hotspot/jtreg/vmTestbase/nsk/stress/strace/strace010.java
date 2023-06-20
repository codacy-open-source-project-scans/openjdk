/*
 * Copyright (c) 2003, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @key stress
 *
 * @summary converted from VM testbase nsk/stress/strace/strace010.
 * VM testbase keywords: [stress, strace]
 * VM testbase readme:
 * DESCRIPTION
 *     The test runs many threads, that recursively invoke a pure java method.
 *     After arriving at defined depth of recursion, each thread is blocked
 *     on entering a monitor. Then the test calls java.lang.Thread.getStackTrace()
 *     and java.lang.Thread.getAllStackTraces() methods and checks their results.
 *     The test fails if:
 *     - amount of stack trace elements and stack trace elements themselves are
 *       the same for both methods;
 *     - there is at least one element corresponding to invocation of unexpected
 *       method. Expected methods are Thread.sleep(), Thread.run() and the
 *       recursive method.
 *
 * @library /vmTestbase
 *          /test/lib
 * @run main/othervm nsk.stress.strace.strace010
 */

package nsk.stress.strace;

import java.util.Map;

/**
 * The test runs <code>THRD_COUNT</code> instances of <code>strace010Thread</code>,
 * that recursively invoke a pure java method. After arriving at defined depth
 * <code>DEPTH</code> of recursion, each thread is blocked on entering a monitor.
 * Then the test calls <code>java.lang.Thread.getStackTrace()</code> and
 * <code>java.lang.Thread.getAllStackTraces()</code> methods and checks their results.
 */
public class strace010 extends StraceBase {

    static final int DEPTH = 500;
    static final int THRD_COUNT = 100;
    static Object lockedObject = new Object();
    static volatile boolean isLocked = false;

    volatile int achivedCount = 0;
    strace010Thread[] threads;

    public static void main(String[] args) {

        strace010 test = new strace010();
        boolean res = true;

        test.startThreads();

        res = test.makeSnapshot();

        test.finishThreads();

        if (!res) {
            new RuntimeException("***>>>Test failed<<<***");
        }

        display(">>>Test passed<<<");
    }

    void startThreads() {
        threads = new strace010Thread[THRD_COUNT];
        achivedCount = 0;

        String tmp_name;
        display("starting threads...");
        for (int i = 0; i < THRD_COUNT; i++) {
            tmp_name = "strace010Thread" + Integer.toString(i);
            threads[i] = new strace010Thread(this, tmp_name);
            threads[i].start();
        }

        waitFor("the defined recursion depth ...");
    }

    void waitFor(String msg) {
        if (msg.length() > 0)
            display("waiting for " + msg);

        while (achivedCount < THRD_COUNT) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                complain("" + e);
            }
        }
        achivedCount = 0;
    }

    boolean makeSnapshot() {

        Map<Thread, StackTraceElement[]> traces;
        int count;
        StackTraceElement[][] elements;
        display("locking object...");
        synchronized (strace010.lockedObject) {
            isLocked = true;
            synchronized (this) {
                notifyAll();
            }
            Thread.currentThread().yield();
            waitFor("");

            display("making all threads snapshots...");
            traces = Thread.getAllStackTraces();
            count = traces.get(threads[0]).length;

            display("making snapshots of each thread...");
            elements = new StackTraceElement[THRD_COUNT][];
            for (int i = 0; i < THRD_COUNT; i++) {
                elements[i] = threads[i].getStackTrace();
            }
        }
        display("object unlocked");

        display("");

        display("checking lengths of stack traces...");
        StackTraceElement[] all;
        for (int i = 1; i < THRD_COUNT; i++) {
            all = traces.get(threads[i]);
            int k = all.length;
            if (count - k > 2) {
                complain("wrong lengths of stack traces:\n\t"
                        + threads[0].getName() + ": " + count
                        + "\t"
                        + threads[i].getName() + ": " + k);
                return false;
            }
        }

        display("checking stack traces...");
        boolean res = true;
        for (int i = 0; i < THRD_COUNT; i++) {
            all = traces.get(threads[i]);
            if (!checkTraces(threads[i].getName(), elements[i], all)) {
                res = false;
            }
        }
        return res;
    }

    boolean checkTraces(String threadName, StackTraceElement[] threadSnap,
                        StackTraceElement[] allSnap) {

        int checkedLength = threadSnap.length < allSnap.length ?
                threadSnap.length : allSnap.length;
        boolean res = true;

        for (int j = 0; j < checkedLength; j++) {
            if (!checkElement(threadSnap[j])) {
                complain("Unexpected " + j + "-element:");
                complain("\tmethod name: " + threadSnap[j].getMethodName());
                complain("\tclass name: " + threadSnap[j].getClassName());
                if (threadSnap[j].isNativeMethod()) {
                    complain("\tline number: (native method)");
                } else {
                    complain("\tline number: " + threadSnap[j].getLineNumber());
                    complain("\tfile name: " + threadSnap[j].getFileName());
                }
                complain("");
                res = false;
            }
        }
        return res;
    }

    void finishThreads() {
        try {
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].isAlive()) {
                    display("waiting for finish " + threads[i].getName());
                    threads[i].join(waitTime);
                }
            }
        } catch (InterruptedException e) {
            complain("" + e);
        }
        isLocked = false;
    }

}

class strace010Thread extends Thread {

    private int currentDepth = 0;

    static int[] arr = new int[1000];
    strace010 test;

    strace010Thread(strace010 test, String name) {
        this.test = test;
        setName(name);
    }

    public void run() {
        try {
            recursiveMethod(arr);
        } catch (Throwable throwable) {
            System.err.println("# ERROR: " + getName() + ": " + throwable);
            System.exit(1);
        }
    }

    void recursiveMethod(int[] arr) {
        currentDepth++;

        if (strace010.DEPTH - currentDepth > 0) {
            recursiveMethod(arr);
        }

        if (strace010.DEPTH == currentDepth) {

            synchronized (test) {
                test.achivedCount++;
            }

            int alltime = 0;
            while (!test.isLocked) {
                synchronized (test) {
                    try {
                        test.wait(1);
                        alltime++;
                    } catch (InterruptedException e) {
                        strace010.complain("" + e);
                    }
                    if (alltime > strace010.waitTime) {
                        throw new RuntimeException("out of wait time");
                    }
                }
            }

            strace010.display(getName() + ">entering to monitor");
            synchronized (test) {
                test.achivedCount++;
            }
            synchronized (strace010.lockedObject) {
                strace010.display(getName() + ">exiting from monitor");
            }
        }

        currentDepth--;
    }
}
