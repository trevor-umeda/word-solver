import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MultiWordMatchCallable implements Callable<List<AnswerSet>> {

    private Map<Integer, List<String>> dictionary;
    private String input;
    private List<Integer> lengthList;
    private List<String> currentAnswerList;
    private List<AnswerSet> possibleAnswers;
    private char[][] grid;

    private int leniencyBoundary = 3;

    public MultiWordMatchCallable(Map<Integer, List<String>> dictionary, String input, List<Integer> lengthList,
                                  List<String> currentAnswerList, List<AnswerSet> possibleAnswers, char[][] grid) {
        this.dictionary = dictionary;
        this.input = input;
        this.lengthList = lengthList;
        this.currentAnswerList = currentAnswerList;
        this.possibleAnswers = new ArrayList<AnswerSet>();
        this.possibleAnswers.addAll(possibleAnswers);
        this.grid = grid;
    }

    @Override
    public List<AnswerSet> call() throws Exception {
        checkMultiWordMatch(this.input, this.lengthList, this.currentAnswerList, this.possibleAnswers);
        return this.possibleAnswers;
    }

    private void checkMultiWordMatch(String input, List<Integer> lengthList, List<String> currentAnswerList, List<AnswerSet> possibleAnswers) {
        int length = lengthList.get(0);
        List<String> wordSet = dictionary.get(length);
        for(String word : wordSet) {
            String remainingInput = checkSingleWordMatch(input, word);
            if(remainingInput != null) {
                List<String> newAnswerList = new ArrayList<String>();

                    newAnswerList.addAll(currentAnswerList);
                    newAnswerList.add(word);
                if(lengthList.size() <= leniencyBoundary || validAnswerSet(grid, new AnswerSet(newAnswerList))) {
                    if (lengthList.size() > 1) {
                        List<Integer> poppedList = new ArrayList<Integer>();
                        poppedList.addAll(lengthList.subList(1, lengthList.size()));
                        checkMultiWordMatch(remainingInput, poppedList, newAnswerList, possibleAnswers);
                    } else {
                        AnswerSet newAnswer = new AnswerSet(newAnswerList);
                        //                    newAnswer.addAnswer(word);
                        possibleAnswers.add(newAnswer);
                    }
                }
            }
        }
    }
    private static String checkSingleWordMatch(String input, String word){

        for(int i = 0; i < word.length(); i++) {
            if(input.contains(word.charAt(i) + "")) {
                StringBuilder sb = new StringBuilder(input);
                sb.deleteCharAt(input.indexOf(word.charAt(i)));
                input = sb.toString();
            }
            else {
                return null;
            }
        }
        return input;
    }
    private boolean validAnswerSet(char[][] grid, AnswerSet potentialAnswer) {
        char[][] newGrid = grid;
        int length = grid.length * grid.length;
        List<GridCoordinate> gridCoordinates;
        String answer = "";

        for(String wordAnswer : potentialAnswer.getAnswers()) {
            answer += wordAnswer + " ";
        }
        answer = answer.trim();
        boolean foundAnswer = false;

        char character = answer.charAt(0);
        for(int j = 0; j < length; j++) {
            gridCoordinates = new ArrayList<GridCoordinate>();

            if(character == grid[j/grid.length][j%grid.length]) {
                GridCoordinate coordinate = new GridCoordinate(j/grid.length, j %grid.length);
                gridCoordinates.add(coordinate);
                newGrid = simulateWordSelect(gridCoordinates, grid, answer, 0);
                if(newGrid != null) {
                    foundAnswer = true;
                }
            }
        }
//            }
        if(!foundAnswer) {
            return foundAnswer;
        }

        return true;
    }

    private char[][] simulateWordSelect(List<GridCoordinate> gridCoordinates, char[][] grid, String wordAnswer, int index) {
        int length = grid.length * grid.length;
        int newIndex = index+1;

        if(newIndex <= wordAnswer.length()-1) {
            char character = wordAnswer.charAt(newIndex);
            if(character == ' ') {
                // Delete from grid if word had a plausible trace through grid
                //
                for(GridCoordinate gridCoordinate : gridCoordinates) {
                    grid[gridCoordinate.getX()][gridCoordinate.getY()] = 0;
                }
                // Make everything collapse vertically fine
                //
                gridCoordinates = new ArrayList<GridCoordinate>();
                // For every column
                //
                for(int j = 0; j < grid.length; j++) {
                    // Go down the column starting at the top
                    //
                    for(int i = grid.length - 1; i > 0 ; i--) {
                        int zeroCount = 0;
                        // If you see a 0
                        //
                        if(grid[i][j] == 0) {
                            // Coumnt how many zeros there are above it
                            //
                            zeroCount++;
                            for(int k = i-1; k >= 0; k--) {
                                if(grid[k][j] == 0) {
                                    zeroCount++;
                                }
                                else {
                                    break;
                                }
                            }
                            // Move the item above downwards for how many zeroes there were
                            //
                            if(i - zeroCount>=0) {
                                grid[i][j] = grid[i-zeroCount][j];
                                grid[i-zeroCount][j] = 0;
                            }
                        }
                    }
                }
                // For every column
                //
                for(int j = 0; j < grid.length-1; j++) {

                    boolean clearedColumn = true;
                    for(int i = 0; i < grid.length ; i++) {
                        if(grid[i][j] != 0) {
                            clearedColumn = false;
                        }
                    }
                    if(clearedColumn) {
                        for(int k = 0; k < grid.length ; k++) {
                            grid[k][j] = grid[k][j+1];
                            grid[k][j+1] = 0;
                        }
                    }

                }

                newIndex += 1;
                character = wordAnswer.charAt(newIndex);
            }

            boolean found = false;
            for(int j = 0; j < length; j++) {
                if(character == grid[j/grid.length][j%grid.length]) {
                    GridCoordinate coordinate = new GridCoordinate(j/grid.length, j %grid.length);
                    // Make sure theres no collisions
                    //
                    if(gridCoordinates.contains(coordinate)) {
                        continue;
                    }

                    // Make sure its within one spot
                    //
                    if(gridCoordinates.size() > 0) {
                        GridCoordinate lastCoordinate = gridCoordinates.get(gridCoordinates.size()-1);
                        if(Math.abs(lastCoordinate.getX() - coordinate.getX()) > 1 || Math.abs(lastCoordinate.getY() - coordinate.getY()) > 1) {
                            continue;
                        }
                    }

                    gridCoordinates.add(coordinate);
                    if(simulateWordSelect(gridCoordinates, copyOf(grid), wordAnswer, newIndex) == null) {
                        gridCoordinates.remove(gridCoordinates.size()-1);
                        continue;
                    }
                    found = true;
                }
            }
            if(!found) {
                return null;
            }

        }

        return grid;
    }
    public char[][] copyOf(char[][] original) {
        char[][] copy = new char[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }
}