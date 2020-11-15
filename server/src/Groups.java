import java.util.ArrayList;

public class Groups {
    private ArrayList<Group> groups;

    public Groups() {
        this.groups = new ArrayList<Group>();
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void add(Group group) {
        this.groups.add(group);
    }

    public void remove(Group group){
        this.groups.remove(group);
    }

    @Override
    public String toString() {
        String str = "";
        str += "Groups{";
        for (Group group: groups) {
            str += group.getName();
            str += ", ";
        }
        str += "}";
        return str;
    }
}
