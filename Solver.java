import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Solver {
    public static Map<Integer, List<String>> dictionary;

    public static void main(String[] args) {
        BufferedReader br = null;
        dictionary = new HashMap<Integer, List<String>>();
        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader("/Users/tumeda/src/personal/src/en_US.txt"));

            while ((sCurrentLine = br.readLine()) != null) {
                String word = sCurrentLine.split("/")[0];
                if(dictionary.get(word.length()) == null) {
                    dictionary.put(word.length(), new ArrayList<String>());
                }
                dictionary.get(word.length()).add(word.toLowerCase());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        List<String> s = dictionary.get(4);
        for(String se : s) {
            if(se.equals("eris")) {
//                System.out.println("Found eris");
            }
        }
        List<AnswerSet> possibleAnswers = new ArrayList<AnswerSet>();
        String input = "pmoetehuafnpmrtctikliksed";
        int lengthOfWord1 = 4;
        int lengthOfWord2 = 5;

        List<Integer> lengthList = Arrays.asList(4,6,7,4,4);
        int numOfWords = lengthList.size();
        System.out.println(" -- Finished Compiling Dictionary --");
        System.out.println("");
        if(numOfWords > 1) {
            possibleAnswers = checkInitialMultiWordMatch(input, lengthList);
            System.out.println(" -- Finished Creating Initial Answers -- ");
            System.out.println(" -- Starting Pruning Answers -- ");
            possibleAnswers = pruneBadAnswerSet(possibleAnswers, numOfWords);
            System.out.println(" -- Finished Pruning Initial Answers -- ");
            List<AnswerSet> possibleAnswers2 = attemptSolve(input, possibleAnswers);
            if(possibleAnswers2.size() > 0 ) {
                possibleAnswers = possibleAnswers2;
            }
            else {
                System.out.println("Failed to find solution through solving.");
            }
        }
        else {
            input = "bdela";
            int length = 5;
            List<String> wordSet = dictionary.get(length);
            for(String word : wordSet) {
                if(checkSingleWordMatch(input, word) != null) {
                    AnswerSet answer = new AnswerSet();
                    answer.addAnswer(word);
                    possibleAnswers.add(answer);
                }
            }
        }
        for(AnswerSet answer : possibleAnswers) {
            System.out.println(answer);
        }
    }

    private static List<AnswerSet> attemptSolve(String input, List<AnswerSet> possibleAnswers) {
        List<AnswerSet> testedAnswers = new ArrayList<AnswerSet>();
        for(AnswerSet potentialAnswer : possibleAnswers) {
            char[][] grid = resetGrid(input);
            if(validAnswerSet(grid, potentialAnswer)) {
                testedAnswers.add(potentialAnswer);
            }
        }
        return testedAnswers;
    }

    private static boolean validAnswerSet(char[][] grid, AnswerSet potentialAnswer) {
        char[][] newGrid = grid;
        int length = grid.length * grid.length;
        List<GridCoordinate> gridCoordinates;
        String answer = "";

        for(String wordAnswer : potentialAnswer.getAnswers()) {
            answer += wordAnswer + " ";
        }
        answer = answer.trim();
        boolean foundAnswer = false;
        if(answer.equals("table record thorn")) {
            System.out.println("SOUTH FOUND");
        }

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

    private static char[][] simulateWordSelect(List<GridCoordinate> gridCoordinates, char[][] grid, String wordAnswer, int index) {
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
    public static char[][] copyOf(char[][] original) {
        char[][] copy = new char[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }
    private static char[][] resetGrid(String input) {
        int dimension = (int)Math.sqrt(input.length());
        char[][] grid = new char[dimension][dimension];
        for(int i = 0; i < input.length(); i++) {
            grid[i/dimension][i%dimension] = input.charAt(i);
        }
        return grid;
    }
    private static List<AnswerSet> pruneBadAnswerSet(List<AnswerSet> possibleAnswers, int numOfWords) {
        Iterator<AnswerSet> iterator = possibleAnswers.iterator();
        while(iterator.hasNext()) {
            if(iterator.next().getAnswerSetSize() < numOfWords) {
                iterator.remove();
            }
        }
        return possibleAnswers;
    }

    private static List<AnswerSet> checkInitialMultiWordMatch(String input, List<Integer> lengthList) {
        int length = lengthList.get(0);
        List<AnswerSet> possibleAnswers = new ArrayList<AnswerSet>();
        List<String> wordSet = dictionary.get(length);
        int test = 0;
        //Get ExecutorService from Executors utility class, thread pool size is 10
        ExecutorService executor = Executors.newFixedThreadPool(10);
        //create a list to hold the Future object associated with Callable
        List<Future<List<AnswerSet>>> list = new ArrayList<Future<List<AnswerSet>>>();
        char[][] grid = resetGrid(input);
        for(String word : wordSet) {
            String remainingInput = checkSingleWordMatch(input, word);
            if(remainingInput != null) {
                if(validAnswerSet(grid, new AnswerSet(Arrays.asList(word)))) {
                    List<Integer> poppedList = new ArrayList<Integer>();
                    poppedList.addAll(lengthList.subList(1, lengthList.size()));
                    MultiWordMatchCallable callable = new MultiWordMatchCallable(dictionary, remainingInput, poppedList, Arrays.asList(word), possibleAnswers, grid);
                    Future<List<AnswerSet>> future = executor.submit(callable);
                    //add Future to the list, we can get return value using Future
                    list.add(future);
//                checkMultiWordMatch(remainingInput, poppedList, Arrays.asList(word), possibleAnswers);
                    test++;
                }

            }
        }
        System.out.println("Initial word matches = " + test);
        for(Future<List<AnswerSet>> fut : list){
            try {
                //print the return value of Future, notice the output delay in console
                // because Future.get() waits for task to get completed
                System.out.println(new Date()+ "::"+fut.get());
                possibleAnswers.addAll(fut.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //shut down the executor service now
        executor.shutdown();

        return possibleAnswers;
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
}
