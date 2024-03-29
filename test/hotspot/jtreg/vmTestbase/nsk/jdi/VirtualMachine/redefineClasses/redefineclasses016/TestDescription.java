/*
 * Copyright (c) 2018, 2024, Oracle and/or its affiliates. All rights reserved.
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
 *
 * @summary converted from VM Testbase nsk/jdi/VirtualMachine/redefineClasses/redefineclasses016.
 * VM Testbase keywords: [quick, jpda, jdi, redefine]
 * VM Testbase readme:
 * DESCRIPTION:
 *  The test against the method com.sun.jdi.VirtualMachine.redefineClasses()
 *  and checks up the following assertion:
 *   "If canUnrestrictedlyRedefineClasses() is false,
 *   changing the schema(the fields) will throw UnsupportedOperationException
 *   exception"
 *  On debugee part there is an interface that has three method declarations:
 *        static public final Object field001
 *               public final Object field002
 *                      final Object field003
 *                            Object field004
 *  JLSS 9.3 says "...every field declaration in the body of an interface is
 *  implicitly public, static and final..."
 *  The test tries to redefine this interface, manipulating modifiers of
 *  these fields, and expects UnsupportedOperationException will not be thrown.
 *  The test consists of the following files:
 *     redefineclasses016.java             - debugger
 *     redefineclasses016a.java            - debuggee
 *     redefineclasses016b.java            - an initial redefined class
 *     newclassXX/redefineclasses016b.java - redefining debuggee's class
 *  This test performs the following cases:
 *     1. newclass01 - removing <static> modifiers
 *     2. newclass02 - adding <static> modifiers
 *     3. newclass03 - removing <final> modifiers
 *     4. newclass04 - adding <final> modifiers
 *     5. newclass05 - removing <public> modifiers
 *     6. newclass06 - adding <public> modifiers
 *  The test checks two different cases for suspended debugee and not
 *  suspended one.
 *  When canRedefineClasses() is false, the test is considered as passed and
 *  completes it's execution.
 * COMMENTS:
 *
 * @library /vmTestbase
 *          /test/lib
 * @build nsk.jdi.VirtualMachine.redefineClasses.redefineclasses016
 *        nsk.jdi.VirtualMachine.redefineClasses.redefineclasses016a
 *
 * @comment compile newclassXX to bin/newclassXX
 *          with full debug info
 * @run driver nsk.share.ExtraClassesBuilder
 *      -g:lines,source,vars
 *      newclass01 newclass02 newclass03 newclass04 newclass05 newclass06
 *
 * @run driver
 *      nsk.jdi.VirtualMachine.redefineClasses.redefineclasses016
 *      ./bin
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      -debugee.vmkeys="${test.vm.opts} ${test.java.opts}"
 */

