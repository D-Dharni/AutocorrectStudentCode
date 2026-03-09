public class Word {

    // Each word should have their string and the edit distance saved

    private String str;
    private int editDistance;

    public Word( String str, int editDistance) {
        this.str = str;
        this.editDistance = editDistance;
    }

    // Getters & Setters

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public int getEditDistance() {
        return editDistance;
    }

    public void setEditDistance(int editDistance) {
        this.editDistance = editDistance;
    }
}
