package liubaoyua.customtext.entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * Created by liubaoyua on 2015/6/19 0019.
 * this class is for customtext.
 */
public class CustomText {
    public String oriText = "";
    public String newText = "";
    public Pattern oriPattern;

    public boolean isCheck = false;


    public CustomText() {
    }

    public CustomText(String oriText, String newText) {
        this.oriText = oriText;
        this.newText = newText;
    }

    public CustomText(CustomText text) {
        this.oriText = text.oriText;
        this.newText = text.newText;
        this.oriPattern = text.oriPattern;
    }

    public Pattern createOriginPattern() {
        try {
            oriPattern = Pattern.compile(oriText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oriPattern;
    }

    public Pattern getPattern() {
        if (oriPattern == null) {
            createOriginPattern();
        }
        return oriPattern;
    }

    public String getOriText() {
        return oriText;
    }

    public String getNewText() {
        return newText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomText that = (CustomText) o;

        if (!oriText.equals(that.oriText)) return false;
        return newText.equals(that.newText);

    }

    @Override
    public int hashCode() {
        int result = oriText.hashCode();
        result = 31 * result + newText.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CustomText{" +
                "oriText='" + oriText + '\'' +
                ", newText='" + newText + '\'' +
                '}';
    }

    public boolean isEmpty() {
        if (this.newText == null || this.newText.equals("")) {
            if (this.oriText == null || this.oriText.equals("")) {
                return true;
            }
        }
        return false;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("ori", oriText).put("new", newText);
        return o;
    }

    public static CustomText fromJsonObject(JSONObject o) {
        CustomText text = new CustomText();
        text.oriText = o.optString("ori");
        text.newText = o.optString("new");
        return text;
    }


}
