import java.util.ArrayList;
import java.util.List;

public class AnswerSet {

    public List<String> answers;

    public AnswerSet(){}

    public AnswerSet(List<String> answers) {
        this.answers = answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }
    public List<String> getAnswers() {
        return this.answers;
    }
    public int getAnswerSetSize() {
        return answers.size();
    }
    public void addAnswer(String answer) {
        if(answers == null) {
            answers = new ArrayList<String>();
        }
        answers.add(answer);
    }

    @Override
    public String toString() {
        String returnString = "";
        for(String answer : answers) {
            returnString += answer + " ";
        }
        return returnString;
    }
}
