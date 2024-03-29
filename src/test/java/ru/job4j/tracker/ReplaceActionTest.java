package ru.job4j.tracker;

import org.junit.Before;
import org.junit.Test;
import ru.job4j.tracker.actions.ReplaceAction;
import ru.job4j.tracker.input.Input;
import ru.job4j.tracker.models.Item;
import ru.job4j.tracker.output.Output;
import ru.job4j.tracker.stubs.StubOutput;
import ru.job4j.tracker.trackers.MemTracker;
import ru.job4j.tracker.trackers.Store;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReplaceActionTest {

    private Store tracker;

    private Output out;

    @Before
    public void whenSetUp() {
        out = new StubOutput();
        tracker = new MemTracker();
        tracker.init();
    }

    @Test
    public void whenReplaceItemSuccess() {
        tracker.add(new Item("Replaced item"));
        String replacedName = "New item name";
        ReplaceAction rep = new ReplaceAction(out);
        Input input = mock(Input.class);
        when(input.askInt(any(String.class))).thenReturn(1);
        when(input.askString(any(String.class))).thenReturn(replacedName);
        rep.execute(input, tracker);
        String ln = System.lineSeparator();
        assertThat(out.toString(),
                is("=== Replace a current Item ===" + ln + "Replace success!" + ln));
        assertThat(tracker.findAll().get(0).getName(), is(replacedName));
    }

    @Test
    public void whenReplaceItemWithError() {
        String itemName = "Replaced item";
        tracker.add(new Item(itemName));
        ReplaceAction rep = new ReplaceAction(out);
        Input input = mock(Input.class);
        when(input.askString(any(String.class))).thenReturn("");
        rep.execute(input, tracker);
        String ln = System.lineSeparator();
        assertThat(out.toString(),
                is("=== Replace a current Item ===" + ln + "Error!" + ln));
        assertThat(tracker.findAll().get(0).getName(), is(itemName));
    }
}