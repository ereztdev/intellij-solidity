package me.serce.solidity.lang.core.resolve

import com.intellij.openapi.util.RecursionManager
import me.serce.solidity.lang.psi.SolFunctionCallExpression
import me.serce.solidity.lang.psi.SolNamedElement

class SolEventResolveTest : SolResolveTestBase() {

  fun testEventWithNoArguments() = checkByCode(
    """
        contract B {
            event Closed();
                    //x
            function close() {
                Closed();
                //^
            }
        }
  """
  )

  fun testEventParent() = checkByCode(
    """
        contract A {
            event Closed();
                    //x
        }

        contract B is A {
            function close() {
                Closed();
                //^
            }
        }
  """
  )

  fun testEventWithParemeters() = checkByCode(
    """
        contract B {
            event Refunded(int a, uint256 b);
                    //x

            function close() {
                Refunded(1, 2);
                //^
            }
        }
  """
  )

  fun testContractTheSameNameAsEvent() {
    // TODO: the test below fails on the latest Solidity compiler with
    //    > SyntaxError: Functions are not allowed to have the same name as the contract.
    //    > If you intend this to be a constructor, use "constructor(...) { ... }" to define it.
    //    In this situation, resolve works incorrectly. However, what the test actually
    //    verifies is that  a StackOverflowException isn't thrown,
    //    see https://github.com/intellij-solidity/intellij-solidity/issues/309
    // A recursion guard check is disabled to prevent c.i.o.u.RecursionManager.CachingPreventedException
    // from being thrown. The case highlights another issue that's worth fixing.
    RecursionManager.disableMissedCacheAssertions(testRootDisposable)
    checkByCode(
      """
        contract B {
            event B();
                   
            function close() public {
                emit B();
                   //^
            }
            
            function B() {
                   //x
            }
        }
    """
    )
  }


  override fun checkByCode(code: String) {
    checkByCodeInternal<SolFunctionCallExpression, SolNamedElement>(code)
  }
}
