package kr.merutilm.rff.ui;

public class HTMLStringBuilder {

    private final StringBuilder builder;

    public enum Tag{
        BOLD("b"),
        ;
        private final String str;
        Tag(String str){
            this.str = str;
        }

        public String start(){
            return "<" + str + ">";
        }
        public String end(){
            return "</" + str + ">";
        }
    }

    public HTMLStringBuilder(){
        this("");
    }

    public HTMLStringBuilder(String s){
        this.builder = new StringBuilder(s);
    }

    public HTMLStringBuilder appendln(Object str){
        return append(str).lineBreak();
    }


    public HTMLStringBuilder append(Object str){
        builder.append(str);
        return this;
    }

    public HTMLStringBuilder lineBreak(){
        builder.append("<br>");
        return this;
    }

    public HTMLStringBuilder wrap(Tag tag, Object str){
        builder.append(tag.start()).append(str).append(tag.end());
        return this;
    }
    
    public HTMLStringBuilder wrapln(Tag tag, Object str){
        return wrap(tag, str).lineBreak();
    }

    @Override
    public String toString(){
        return builder.insert(0, "<html>").append("</html>").toString();
    }
}