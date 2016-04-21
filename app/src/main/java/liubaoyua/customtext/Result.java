package liubaoyua.customtext;

/**
 * Created by liubaoyua on 2016/4/21.
 */
public class Result {
    private boolean change;
    private String text;

    public Result(String text, boolean change) {
        this.text = text;
        this.change = change;
    }

    public boolean isChange() {
        return change;
    }

    public void setText(String text) {
        this.text = text;
        this.change = true;
    }

    public String getText() {
        return text;
    }
}
