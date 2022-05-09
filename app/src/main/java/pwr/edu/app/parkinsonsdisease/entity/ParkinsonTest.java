package pwr.edu.app.parkinsonsdisease.entity;

public class ParkinsonTest {
    private String id;
    private String name;
    private String imageName;
    private String className;
    private String packageName;
    private boolean isSelected;

    public ParkinsonTest() {
    }

    public ParkinsonTest(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getImageName() {
        imageName = name.toLowerCase();
        if(imageName.contains(" ")){
            String[] strings = imageName.split(" ");
            imageName = strings[0] + "_" + strings[1];
        }

        return imageName;
    }

    public String getClassName() {
        className = name;
        String[] strArray = className.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap);
        }
        className = builder.toString();
        return className;
    }

    public String getPackageName() {
        packageName = "";
        String[] strArray = name.toLowerCase().split(" ");
        for (String s : strArray) {
            packageName += s;
        }
        return packageName;
    }
}
