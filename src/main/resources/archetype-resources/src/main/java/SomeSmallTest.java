#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.java.annotation.GraphWalker;

@GraphWalker(start="e_AnotherAction")
public class SomeSmallTest extends ExecutionContext implements SmallTest {

    @Override
    public void e_AnotherAction() {
        System.out.println("Running: e_AnotherAction");
    }

    @Override
    public void e_SomeAction() {
        System.out.println("Running: e_SomeAction");
    }

    @Override
    public void e_SomeOtherAction() {
        System.out.println("Running: e_SomeOtherAction");
    }

    @Override
    public void v_VerifySomeAction() {
        System.out.println("Running: v_VerifySomeAction");
    }

    @Override
    public void v_VerifySomeOtherAction() {
        System.out.println("Running: v_VerifySomeOtherAction");
    }
}
