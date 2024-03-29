package ru.job4j.tracker;

import org.junit.Before;
import org.junit.Test;
import ru.job4j.tracker.input.Input;
import ru.job4j.tracker.input.ValidateInput;
import ru.job4j.tracker.output.Output;
import ru.job4j.tracker.stubs.StubInput;
import ru.job4j.tracker.stubs.StubOutput;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ValidateInputTest {

    private Output out;

    @Before
    public void whenSetUp() {
        out = new StubOutput();
    }

    @Test
    public void whenInvalidInput() {
        Input in = new StubInput(new String[] {"one", "1"});
        ValidateInput input = new ValidateInput(out, in);
        int selected = input.askInt("Enter menu:");
        assertThat(selected, is(1));
    }

    @Test
    public void whenCorrectInput() {
        Input in = new StubInput(new String[] {"5"});
        ValidateInput input = new ValidateInput(out, in);
        int selected = input.askInt("Enter menu:");
        assertThat(selected, is(5));
    }

    @Test
    public void whenMultipleCorrectInput() {
        Input in = new StubInput(new String[] {"0", "5", "3", "4"});
        ValidateInput input = new ValidateInput(out, in);
        int[] expected = {0, 5, 3, 4};
        for (int exp : expected) {
            int selected = input.askInt("Enter menu:");
            assertThat(selected, is(exp));
        }
    }

    @Test
    public void whenNegativeInput() {
        Input in = new StubInput(new String[] {"-1"});
        ValidateInput input = new ValidateInput(out, in);
        int selected = input.askInt("Enter menu:");
        assertThat(selected, is(-1));
    }
}