/*
 * java-kcoin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-kcoin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kcoin.common.runtime.vm;

import static org.junit.Assert.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kcoin.common.parameter.CommonParameter;
import org.kcoin.common.runtime.InternalTransaction;
import org.kcoin.common.runtime.InternalTransaction.SymType;
import org.kcoin.core.config.args.Args;
import org.kcoin.core.exception.ContractValidateException;
import org.kcoin.core.vm.VM;
import org.kcoin.core.vm.program.Program;
import org.kcoin.core.vm.program.invoke.ProgramInvokeMockImpl;
import org.kcoin.protos.Protocol.Transaction;

@Slf4j
public class InterpreterTest {

  private ProgramInvokeMockImpl invoke;
  private Program program;

  @BeforeClass
  public static void init() {
    CommonParameter.getInstance().setDebug(true);
  }

  @AfterClass
  public static void destroy() {
    Args.clearParam();
  }

  @Test
  public void testVMException() throws ContractValidateException {
    VM vm = new VM();
    invoke = new ProgramInvokeMockImpl();
    byte[] op = {0x5b, 0x60, 0x00, 0x56};
    // 0x5b      - JUMPTEST
    // 0x60 0x00 - PUSH 0x00
    // 0x56      - JUMP to 0
    Transaction sym = Transaction.getDefaultInstance();
    InternalTransaction interSym = new InternalTransaction(sym, SymType.SYM_UNKNOWN_TYPE);
    program = new Program(op, invoke, interSym);

    boolean result = false;

    try {
      while (!program.isStopped()) {
        vm.step(program);
      }
    } catch (Program.OutOfEnergyException e) {
      result = true;
    }

    assertTrue(result);
  }

  @Test
  public void JumpSingleOperation() throws ContractValidateException {
    VM vm = new VM();
    invoke = new ProgramInvokeMockImpl();
    byte[] op = {0x56};
    // 0x56      - JUMP
    Transaction sym = Transaction.getDefaultInstance();
    InternalTransaction interSym = new InternalTransaction(sym, SymType.SYM_UNKNOWN_TYPE);
    program = new Program(op, invoke, interSym);

    boolean result = false;

    try {
      while (!program.isStopped()) {
        vm.step(program);
      }
    } catch (Program.StackTooSmallException e) {
      // except to get stack too small exception for Jump
      result = true;
    }

    assertTrue(result);
  }

  @Test
  public void JumpToInvalidDestination() throws ContractValidateException {
    VM vm = new VM();
    invoke = new ProgramInvokeMockImpl();
    byte[] op = {0x60, 0x20, 0x56};
    // 0x60      - PUSH1
    // 0x20      - 20
    // 0x56      - JUMP
    Transaction sym = Transaction.getDefaultInstance();
    InternalTransaction interSym = new InternalTransaction(sym, SymType.SYM_UNKNOWN_TYPE);
    program = new Program(op, invoke, interSym);

    boolean result = false;

    try {
      while (!program.isStopped()) {
        vm.step(program);
      }
    } catch (Program.BadJumpDestinationException e) {
      // except to get BadJumpDestinationException for Jump
      Assert.assertTrue(e.getMessage().contains("Operation with pc isn't 'JUMPDEST': PC[32];"));
      result = true;
    }

    assertTrue(result);
  }

  @Test
  public void JumpToLargeNumberDestination() throws ContractValidateException {
    VM vm = new VM();
    invoke = new ProgramInvokeMockImpl();
    byte[] op = {0x64, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x56};
    // 0x60              - PUSH5
    // 0x7F7F7F7F7F      - 547599908735
    // 0x56              - JUMP
    Transaction sym = Transaction.getDefaultInstance();
    InternalTransaction interSym = new InternalTransaction(sym, SymType.SYM_UNKNOWN_TYPE);
    program = new Program(op, invoke, interSym);

    boolean result = false;

    try {
      while (!program.isStopped()) {
        vm.step(program);
      }
    } catch (Program.BadJumpDestinationException e) {
      // except to get BadJumpDestinationException for Jump
      Assert.assertTrue(e.getMessage().contains("Operation with pc isn't 'JUMPDEST': PC[-1];"));
      result = true;
    }

    assertTrue(result);
  }
}
