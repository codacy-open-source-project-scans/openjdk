/*
 * Copyright (c) 2001, 2024, Oracle and/or its affiliates. All rights reserved.
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

package nsk.jdi.EventRequestManager.createBreakpointRequest;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Location;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.VMMismatchException;
import java.io.*;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

/**
 * The test checks that the JDI method
 * <b>com.sun.jdi.request.EventRequestManager.createBreakpointRequest()</b>
 * properly throws <code>NullPointerException</code> - if location is null.
 */
public class crbreakpreq002 {
    public static final int PASSED = 0;
    public static final int FAILED = 2;
    public static final int JCK_STATUS_BASE = 95;
    static final String DEBUGGEE_CLASS =
        "nsk.jdi.EventRequestManager.createBreakpointRequest.crbreakpreq002t";
    static final String COMMAND_READY = "ready";
    static final String COMMAND_QUIT = "quit";

    private ArgumentHandler argHandler;
    private Log log;
    private IOPipe pipe;
    private Debugee debuggee;

    public static void main (String argv[]) {
        int result = run(argv,System.out);
        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }
    }

    public static int run(String argv[], PrintStream out) {
        return new crbreakpreq002().runIt(argv, out);
    }

    private int runIt(String args[], PrintStream out) {
        argHandler = new ArgumentHandler(args);
        log = new Log(out, argHandler);
        Binder binder = new Binder(argHandler, log);
        BreakpointRequest bpRequest;
        String cmd;
        Location loc = null;

        debuggee = binder.bindToDebugee(DEBUGGEE_CLASS);
        pipe = debuggee.createIOPipe();
        debuggee.redirectStderr(log, "crbreakpreq002t.err> ");
        VirtualMachine vm = debuggee.VM();
        EventRequestManager erManager = vm.eventRequestManager();
        debuggee.resume();
        cmd = pipe.readln();
        if (!cmd.equals(COMMAND_READY)) {
            log.complain("TEST BUG: unknown debuggee's command: "
                + cmd);
            return quitDebuggee(FAILED);
        }

// Trying to create BreakpointRequest for a null Location parameter
        try {
            bpRequest =
                erManager.createBreakpointRequest(loc);
        } catch (NullPointerException e) {
            log.display("TEST PASSED: EventRequestManager.createBreakpointRequest() throws expected "
                + e);
            return quitDebuggee(PASSED);
        } catch(VMMismatchException e) {
            log.complain("TEST FAILED: EventRequestManager.createBreakpointRequest() throws unexpected "
                + e + "\n\tbut it should throw NullPointerException for a null location");
            return quitDebuggee(FAILED);
        } catch(UnsupportedOperationException e) { // specified only in jdk1.4
            log.complain("WARNING: test has no result. EventRequestManager.createBreakpointRequest() throws "
                + e);
            return quitDebuggee(PASSED);
        }
        log.complain("TEST FAILED: EventRequestManager.createBreakpointRequest() successfully done,\n\t"
            + "but it should throw NullPointerException for a null location");
        return quitDebuggee(FAILED);
    }

    private int quitDebuggee(int stat) {
        pipe.println(COMMAND_QUIT);
        debuggee.waitFor();
        return stat;
    }
}
