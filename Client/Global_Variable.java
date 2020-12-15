public class Global_Variable {
    boolean micStatus;
    public Global_Variable () {
         micStatus = false;
    }
    public void setMicStatus (boolean status) {
        this.micStatus = status;
    }
    public boolean getMicStatus () {
        return this.micStatus;
    }
}
