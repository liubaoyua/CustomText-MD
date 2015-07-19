package liubaoyua.customtext.utils;

/**
 * Created by liubaoyua on 2015/6/19 0019.
 * this class is for customtext.
 */
public class CustomText {
    public String oriText = "";
    public String newText = "";
    public boolean isRegex = false;
    public int textColor = 0 ;
    public boolean underLine = false;
    public boolean isAvailable = true;

    public boolean isWorkInEditText = false;
    public boolean isReplacedToAPic = false;
    public int picMagnification = 1;


    public CustomText(){}

    public CustomText(String oriText, String newText){
        this.oriText=oriText;
        this.newText=newText;
    }

    public CustomText(CustomText text){
        this.oriText=text.oriText;
        this.newText=text.newText;
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

    public boolean isEmpty(){
        if(this.newText==null || this.newText.equals("")){
            if(this.oriText==null || this.oriText.equals("")){
                return true;
            }
        }
        return false;
    }


}
