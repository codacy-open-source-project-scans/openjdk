/*
 * Copyright (c) 2021, Red Hat, Inc. All rights reserved.
 * Copyright (c) 2022, 2023, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

/**
 * @test TestPrimitiveArrayCriticalWithBadParam
 * @bug 8269697 8292674
 * @summary -Xcheck:jni should catch wrong parameter passed to GetPrimitiveArrayCritical
 * @comment Tests reporting with regular thread and virtual thread.
 * @library /test/lib
 * @run main/native TestPrimitiveArrayCriticalWithBadParam
 */
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.Utils;

public class TestPrimitiveArrayCriticalWithBadParam {
    static {
        System.loadLibrary("TestBadParam");
    }

    private static native void pin(Object[] a);
    private static native void unpin(Object[] a);

    public static void main(String[] args) {
        if (args.length > 0) {
            test(args[0]);
        } else {
            runTest(false);
            runTest(true);
        }
    }

    private static void runTest(boolean useVThread) {
        List<String> pbArgs = new ArrayList<>();
        pbArgs.add("-XX:-CreateCoredumpOnCrash");
        pbArgs.add("-Xcheck:jni");
        pbArgs.add("--enable-preview");
        pbArgs.add("-Djava.library.path=" + Utils.TEST_NATIVE_PATH);
        pbArgs.add(TestPrimitiveArrayCriticalWithBadParam.class.getName());
        pbArgs.add(useVThread ? "vtest" : "test");
        try {
            ProcessBuilder pb = ProcessTools.createLimitedTestJavaProcessBuilder(pbArgs.toArray(new String[0]));
            OutputAnalyzer analyzer = new OutputAnalyzer(pb.start());

            // -Xcheck:jni should warn the bad parameter
            analyzer.shouldContain("FATAL ERROR in native method: Primitive type array expected but not received for JNI array operation");
            analyzer.shouldContain("TestPrimitiveArrayCriticalWithBadParam.pin");
            analyzer.shouldNotHaveExitValue(0);
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    private static void test(String mode) {
        Runnable r = () -> {
            Object[] objs = new Object[10];
            for (int i = 0; i < objs.length; i++) {
                objs[i] = new MyClass();
            }
            pin(objs);
            System.out.println("Object array pinned");
            unpin(objs);
        };
        if (mode.equals("vtest")) {
            Thread t = Thread.ofVirtual().start(r);
            try {
                t.join();
            } catch (InterruptedException ex) {
                throw new Error("unexpected", ex);
            }
        } else {
            r.run();
        }
    }
    public static class MyClass {
        public Object ref = new Object();
    }
}
