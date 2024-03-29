/*
 * Copyright (c) 2000, 2024, Oracle and/or its affiliates. All rights reserved.
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

package nsk.jdi.ReferenceType.isPrepared;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

import com.sun.jdi.*;
import java.util.*;
import java.io.*;

/**
 * This test checks the method <code>isPrepared()</code>
 * of the JDI interface <code>ReferenceType</code> of com.sun.jdi package
 */

public class isprepared001 {
    static ArgumentHandler argsHandler;
    static Log test_log_handler;
    static boolean verbose_mode = false;  // test argument -verbose switches to true
                                          // - for more easy failure evaluation

    /** The main class isPrepareds of the debugger & debugee applications. */
    private final static String
        package_prefix = "nsk.jdi.ReferenceType.isPrepared.",
//        package_prefix = "",    //  for DEBUG without package
        thisClassName = package_prefix + "isprepared001",
        debugeeName   = thisClassName + "a";

        static String is_prepared_sign = "is_prepared";
        static String not_prepared_sign = "not_prepared";

    /** Debugee's classes for check **/
    private final static String classes_for_check[][] = {

//        {package_prefix + "NotPreparedClass", not_prepared_sign},
//        {package_prefix + "NotPreparedInterface", not_prepared_sign},

        {package_prefix + "PreparedClass", is_prepared_sign},
        {package_prefix + "PreparedInterface", is_prepared_sign}

    };


    public static void main (String argv[]) {
        int result = run(argv,System.out);
        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }

    }

    /**
     * JCK-like entry point to the test: perform testing, and
     * return exit code 0 (PASSED) or either 2 (FAILED).
     */
    public static int run (String argv[], PrintStream out) {

        int v_test_result = new isprepared001().runThis(argv,out);
        if ( v_test_result == 2/*STATUS_FAILED*/ ) {
            print_log_anyway("\n==> nsk/jdi/ReferenceType/isPrepared/isprepared001 test FAILED");
        }
        else {
            print_log_on_verbose("\n==> nsk/jdi/ReferenceType/isPrepared/isprepared001 test PASSED");
        }
        return v_test_result;
    }

    private static void print_log_on_verbose(String message) {
        test_log_handler.display(message);
    }

    private static void print_log_anyway(String message) {
        test_log_handler.println(message);
    }

    /**
     * Non-static variant of the method <code>run(args,out)</code>
     */
    private int runThis (String argv[], PrintStream out) {

        argsHandler      = new ArgumentHandler(argv);
        test_log_handler = new Log(out, argsHandler);
        Binder binder    = new Binder(argsHandler, test_log_handler);

        print_log_on_verbose("==> nsk/jdi/ReferenceType/isInitialized/isinit001 test LOG:");
        print_log_on_verbose("==> test checks the isInitialized() method of ReferenceType interface");
        print_log_on_verbose("    of the com.sun.jdi package for ClassType, InterfaceType\n");

        String debugee_launch_command = debugeeName;
        if (verbose_mode) {
            debugee_launch_command = debugeeName + " -vbs";
        }

        Debugee debugee = binder.bindToDebugee(debugee_launch_command);
        IOPipe pipe = new IOPipe(debugee);

        debugee.redirectStderr(out);
        print_log_on_verbose("--> isprepared001: isprepared001a debugee launched");
        debugee.resume();

        String line = pipe.readln();
        if (line == null) {
            print_log_anyway("##> isprepared001: UNEXPECTED debugee's signal (not \"ready\") - " + line);
            return 2/*STATUS_FAILED*/;
        }
        if (!line.equals("ready")) {
            print_log_anyway("##> isprepared001: UNEXPECTED debugee's signal (not \"ready\") - " + line);
            return 2/*STATUS_FAILED*/;
        }
        else {
            print_log_on_verbose("--> isprepared001: debugee's \"ready\" signal recieved!");
        }

        print_log_on_verbose
            ("--> isprepared001: check ReferenceType.isPrepared() method for debugee's classes...");
        int all_classes_count = 0;
        int class_not_found_errors = 0;
        int isPrepared_method_exceptions = 0;
        int isPrepared_method_errors = 0;
        for (int i=0; i<classes_for_check.length; i++) {
            String className = classes_for_check[i][0];
            all_classes_count++;
            ReferenceType refType = debugee.classByName(className);
            if (refType == null) {
                print_log_anyway("##> isprepared001: Could NOT FIND class: " + className);
                class_not_found_errors++;
                continue;
            }
            boolean expected_is_prepared_result = classes_for_check[i][1].equals(is_prepared_sign);
            boolean returned_is_prepared_result = false;
            try {
                returned_is_prepared_result = refType.isPrepared();
            }
            catch (Throwable thrown) {
                print_log_anyway
                    ("##> isprepared001: FAILED: refType.isPrepared() threw unexpected exception - "
                    + thrown);
                print_log_anyway
                    ("##>                refType = " + refType);
                isPrepared_method_exceptions++;
                continue;
            }
            if ( returned_is_prepared_result != expected_is_prepared_result ) {
                print_log_anyway
                    ("##> isprepared001: FAILED: ReferenceType.isPrepared() returned unexpected result = "
                    + returned_is_prepared_result);
                print_log_anyway
                    ("##>                checked class = " + className);
                isPrepared_method_errors++;
            }
            else {
                print_log_on_verbose
                    ("--> isprepared001: PASSED: ReferenceType.isPrepared() returned expected result = "
                    + returned_is_prepared_result);
                print_log_on_verbose
                    ("-->                checked class = " + className);
            }
        }
        print_log_on_verbose("--> isprepared001: check completed!");
        print_log_on_verbose("--> isprepared001: number of checked classes = " + all_classes_count);
        if ( class_not_found_errors > 0 ) {
            print_log_anyway("##> isprepared001: \"class not found ERRORS\" number = "
                                + class_not_found_errors);
        }
        if ( isPrepared_method_exceptions > 0 ) {
            print_log_anyway("##> isprepared001: number of unexpected isPrepared() methods exceptions = "
                                + isPrepared_method_exceptions);
        }
        if (isPrepared_method_errors > 0) {
            print_log_anyway("##> isprepared001: isPrepared() method errors number = "
                            + isPrepared_method_errors);
        }
        int v_test_result = 0/*STATUS_PASSED*/;
        if (class_not_found_errors + isPrepared_method_errors + isPrepared_method_exceptions > 0) {
            v_test_result = 2/*STATUS_FAILED*/;
        }

        print_log_on_verbose("--> isprepared001: waiting for debugee finish...");
        pipe.println("quit");
        debugee.waitFor();

        int status = debugee.getStatus();
        if (status != 0/*STATUS_PASSED*/ + 95/*STATUS_TEMP*/) {
            print_log_anyway("##> isprepared001: UNEXPECTED Debugee's exit status (not 95) - " + status);
            v_test_result = 2/*STATUS_FAILED*/;
        }
        else {
            print_log_on_verbose("--> isprepared001: expected Debugee's exit status - " + status);
        }

        return v_test_result;
    }
}
