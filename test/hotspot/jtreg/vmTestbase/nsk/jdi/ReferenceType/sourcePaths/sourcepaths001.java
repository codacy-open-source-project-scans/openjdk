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

package nsk.jdi.ReferenceType.sourcePaths;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

import com.sun.jdi.*;
import java.util.*;
import java.io.*;

/**
 * The test for the implementation of an object of the type     <BR>
 * ReferenceType.                                               <BR>
 *                                                              <BR>
 * The test checks up that results of the method                <BR>
 * <code>com.sun.jdi.ReferenceType.sourcePaths()</code>         <BR>
 * complies with its specification.                             <BR>
 * <BR>
 * The case for testing debuggee's sourcecode file with a predefined name<BR>
 * and a predefined package name.                       <BR>
 * A debugger gets ReferenceType object, mirroring debuggee's Class and <BR>
 * performs the following:                                              <BR>
 * - gets a List returned by the method                                 <BR>
 *    ReferenceType.sourcePaths(defaultStratum);                        <BR>
 * - checks that the List contains only one String element which,       <BR>
 *    is equal to the String which is concatenation of the package name <BR>
 *    and the sourceName.                                               <BR>
 */

public class sourcepaths001 {

    //----------------------------------------------------- templete section
    static final int PASSED = 0;
    static final int FAILED = 2;
    static final int PASS_BASE = 95;

    //----------------------------------------------------- templete parameters
    static final String
    sHeader1 = "\n==> nsk/jdi/ReferenceType/sourcePaths/sourcepaths001 ",
    sHeader2 = "--> debugger: ",
    sHeader3 = "##> debugger: ";

    //----------------------------------------------------- main method

    public static void main (String argv[]) {

        int result = run(argv, System.out);

        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }
    }

    public static int run (String argv[], PrintStream out) {

        return new sourcepaths001().runThis(argv, out);
    }

    //--------------------------------------------------   log procedures

    private static Log  logHandler;

    private static void log1(String message) {
        logHandler.display(sHeader1 + message);
    }
    private static void log2(String message) {
        logHandler.display(sHeader2 + message);
    }
    private static void log3(String message) {
        logHandler.complain(sHeader3 + message);
    }

    //  ************************************************    test parameters

    private String debuggeeName =
        "nsk.jdi.ReferenceType.sourcePaths.sourcepaths001a";

    //String mName = "nsk.jdi.ReferenceType.sourcePaths";

    //====================================================== test program
    //------------------------------------------------------ common section

    static ArgumentHandler      argsHandler;

    static int waitTime;

    static VirtualMachine vm = null;

    static int  testExitCode = PASSED;

    //------------------------------------------------------ methods

    private int runThis (String argv[], PrintStream out) {

        Debugee debuggee;

        argsHandler     = new ArgumentHandler(argv);
        logHandler      = new Log(out, argsHandler);
        Binder binder   = new Binder(argsHandler, logHandler);

        if (argsHandler.verbose()) {
            debuggee = binder.bindToDebugee(debuggeeName + " -vbs");
        } else {
            debuggee = binder.bindToDebugee(debuggeeName);
        }

        waitTime = argsHandler.getWaitTime();


        IOPipe pipe     = new IOPipe(debuggee);

        debuggee.redirectStderr(out);
        log2(debuggeeName + " debuggee launched");
        debuggee.resume();

        String line = pipe.readln();
        if ((line == null) || !line.equals("ready")) {
            log3("signal received is not 'ready' but: " + line);
            return FAILED;
        } else {
            log2("'ready' recieved");
        }

        vm = debuggee.VM();

    //------------------------------------------------------  testing section
        log1("      TESTING BEGINS");

        for (int i = 0; ; i++) {

            pipe.println("newcheck");
            line = pipe.readln();

            if (line.equals("checkend")) {
                log2("     : returned string is 'checkend'");
                break ;
            } else if (!line.equals("checkready")) {
                log3("ERROR: returned string is not 'checkready'");
                testExitCode = FAILED;
                break ;
            }

            log1("new checkready: #" + i);

            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ variable part

            List        sourcePaths = null;
            List        sourceNames = null;
            ReferenceType refType   = null;

            log2("      getting: List classes = vm.classesByName(debuggeeName);");
            List classes = vm.classesByName(debuggeeName);

            if (classes.size() != 1) {
                testExitCode = FAILED;
                log3("ERROR: classes.size() != 1");
                break ;
            }

            log2("      getting a tested ReferenceType object 'refType'");
            refType = (ReferenceType) classes.get(0);

            log2("...... getting : String defaultStratum = vm.getDefaultStratum();");
            String defaultStratum = vm.getDefaultStratum();

            log2("......sourcePaths = refType.sourcePaths(defaultStratum);");
            log2("       no AbsentInformationException is expected");
            try {
                sourcePaths = refType.sourcePaths(defaultStratum);
            } catch ( AbsentInformationException e ) {
                testExitCode = FAILED;
                log3("ERROR: AbsentInformationException");
                break ;
            }

            log2("......checking up on a value of sourcePaths.size(); 1 is expected");
            if (sourcePaths.size() != 1) {
                testExitCode = FAILED;
                log3("ERROR: sourcePaths.size() != 1");
                break ;
            }

            log2("......getting: String sourcePath = sourcePaths.get(0);");
            String sourcePath;
            try {
                sourcePath = (String) sourcePaths.get(0);
                log2("        sourcePath == " + sourcePath);
            } catch ( Exception e ) {
                log3("ERROR: exception thrown : " + e);
                testExitCode = FAILED;
                break ;
            }

            log2("......sourceNames = refType.sourceNames(defaultStratum);");
            log2("       no AbsentInformationException is expected");
            try {
                sourceNames = refType.sourceNames(defaultStratum);
            } catch ( AbsentInformationException e ) {
                testExitCode = FAILED;
                log3("ERROR: AbsentInformationException");
                break ;
            }

            log2("......checking up on a value of sourceNames.size(); 1 is expected");
            if (sourceNames.size() != 1) {
                testExitCode = FAILED;
                log3("ERROR: sourceNames.size() != 1");
                break ;
            }

            log2("......getting: String sourceName = sourceNames.get(0);");
            String sourceName;
            try {
                sourceName = (String) sourceNames.get(0);
                log2("        sourceName == " + sourceName);
            } catch ( Exception e ) {
                log3("ERROR: exception thrown : " + e);
                testExitCode = FAILED;
                break ;
            }

            log2("......forming String debuggeeSourcePath : package name + sourceName");
            String sep = System.getProperty("file.separator");
            String debuggeeSourcePath = "nsk" + sep + "jdi" + sep + "ReferenceType" + sep +
                                                   "sourcePaths" + sep + sourceName;

            log2("......compareing: sourcePath to debuggeeSourcePath");
            if (!sourcePath.equals(debuggeeSourcePath)) {
                log3("ERROR: sourcePath != debuggeeSourcePath");
                log3("       sourcePath         == " + sourcePath);
                log3("       debuggeeSourcePath == " + debuggeeSourcePath);
                testExitCode = FAILED;
            }

            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        }
        log1("      TESTING ENDS");

    //--------------------------------------------------   test summary section
    //-------------------------------------------------    standard end section

        pipe.println("quit");
        log2("waiting for the debuggee to finish ...");
        debuggee.waitFor();

        int status = debuggee.getStatus();
        if (status != PASSED + PASS_BASE) {
            log3("debuggee returned UNEXPECTED exit status: " +
                    status + " != PASS_BASE");
            testExitCode = FAILED;
        } else {
            log2("debuggee returned expected exit status: " +
                    status + " == PASS_BASE");
        }

        if (testExitCode != PASSED) {
             logHandler.complain("TEST FAILED");
        }
        return testExitCode;
    }
}
