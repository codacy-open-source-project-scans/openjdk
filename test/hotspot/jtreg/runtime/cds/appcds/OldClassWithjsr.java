/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
/*
 * @test
 * @bug 8302795
 * @summary CDS support of old classes with major version < JDK_6 (50) for static archive.
 *          Test old class with jsr bytecode.
 * @requires vm.cds
 * @library /test/lib
 * @compile test-classes/OldClassWithjsrApp.jasm
 * @run driver OldClassWithjsr
 */

import jdk.test.lib.cds.CDSTestUtils;
import jdk.test.lib.process.OutputAnalyzer;

public class OldClassWithjsr {
    public static void main(String[] args) throws Exception {
        String mainClass = "OldClassWithjsrApp";
        String namePrefix = "oldclasswithjsr";
        String appClasses[] = TestCommon.list(mainClass);
        JarBuilder.build(namePrefix, appClasses);
        String appJar = TestCommon.getTestJar(namePrefix + ".jar");

        boolean dynamicMode = CDSTestUtils.DYNAMIC_DUMP;

        // create archive with class list
        OutputAnalyzer output = TestCommon.dump(appJar, appClasses, "-Xlog:class+load,cds=debug,verification=trace");
        TestCommon.checkExecReturn(output, 0,
                                   dynamicMode ? true : false,
                                   "Skipping " + mainClass + ": Old class has been linked");

        // run with archive
        TestCommon.run(
            "-cp", appJar,
            "-Xlog:class+load,cds=debug,verification=trace",
            mainClass, "1")
          .assertNormalExit(out -> {
              out.shouldContain("Verifying class " + mainClass + " with old format");
              if (!dynamicMode) {
                  out.shouldContain(mainClass + " source: shared objects file");
              } else {
                  // Old classes were already linked before dynamic dump happened,
                  // so they couldn't be archived.
                  out.shouldMatch(".class.load.*" + mainClass + " source:.*" + namePrefix + ".jar");
              }
          });
    }
}
