package kr.merutilm.rff.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

record RFFMenuTree(List<RFFMenuTree> elements, String name, JMenuItem matcher) {

    public static final class Builder {
        //type 1 : multi elements
        private final List<Builder> elements;
        private final String name;
        //type 2 : single element
        private final JMenuItem matcher;
    
        private Builder(String name){
            this.elements = new ArrayList<>();
            this.name = name;
            this.matcher = null;
        }

        private Builder(JMenuItem item){
            this.elements = null;
            this.name = null;
            this.matcher = item;
        }

        public static Builder init(String name){
            return new Builder(name);
        }

        public Builder createBranch(String name, Consumer<Builder> build){
            Builder b = init(name);
            build.accept(b);
            elements.add(b);
            return this;
        }


        public Builder createFruit(JMenuItem item){
            elements.add(new Builder(item));
            return this;
        }
        

        public RFFMenuTree build(){
            if(elements == null){
                return new RFFMenuTree(null, null, matcher);
            }else{
                return new RFFMenuTree(elements.stream().map(Builder::build).toList(), name, null);
            }
        }
    }

    public JMenuItem createUI(){
        return createUI(true);
    }
    
    private JMenuItem createUI(boolean head){
        if(elements == null){
            return matcher;
        }else{
            JMenu menu = head ? new RFFMenuBarMenu(name) : new RFFMenu(name);
            elements.forEach(e -> menu.add(e.createUI(false)));
            return menu;
        }
    }
}
