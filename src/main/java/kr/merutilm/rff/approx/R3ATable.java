package kr.merutilm.rff.approx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.merutilm.rff.formula.MandelbrotReference;
import kr.merutilm.rff.functions.ArrayCompressionTool;
import kr.merutilm.rff.functions.ArrayCompressor;
import kr.merutilm.rff.settings.R3ACompressionMethod;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.util.ArrayFunction;

public abstract class R3ATable {

    protected final R3ASettings settings;
    protected final ArrayCompressor pulledR3ACompressor;
    protected final Period r3aPeriod;
    
    protected R3ATable(MandelbrotReference reference, R3ASettings r3aSettings){
        int[] referencePeriod = reference.period();
        int longestPeriod = reference.longestPeriod();
        int minSkip = r3aSettings.minSkipReference();
        
        if(longestPeriod < minSkip){
            this.settings = r3aSettings;
            this.r3aPeriod = null;
            this.pulledR3ACompressor = null;
            return;
        }

        R3ACompressionMethod compressionMethod = r3aSettings.r3aCompressionMethod();
        Period r3aPeriod = R3ATable.Period.create(referencePeriod, r3aSettings);
        ArrayCompressor pulledR3ACompressor = compressionMethod == R3ACompressionMethod.STRONGEST ? createPulledR3ACompressor(r3aPeriod, reference.referenceCompressor()) : null;
        
        this.settings = r3aSettings;
        this.r3aPeriod = r3aPeriod;
        this.pulledR3ACompressor = pulledR3ACompressor;
        
    }

    public abstract int length();

    private static ArrayCompressor createPulledR3ACompressor(Period r3aPeriod, ArrayCompressor refCompressor){

        List<ArrayCompressionTool> refCompTools = refCompressor.tools();
        List<ArrayCompressionTool> r3aTools = new ArrayList<>();
        int[] tablePeriod = r3aPeriod.tablePeriod;
        int[] tablePeriodElements = r3aPeriod.tableElements;
        int[] requiredPerturbation = r3aPeriod.requiredPerturbation;


        for (int i = 0; i < refCompTools.size(); i++) {
            ArrayCompressionTool tool = refCompTools.get(i);
            int start = tool.start();
            int length = tool.range();
            int index = Arrays.binarySearch(tablePeriod, length + 1);
            //if the reference compressor is same as period
            if(index >= 0 && requiredPerturbation[index] == Period.getRequiredPerturbationCount(false)){
                int tableIndex = iterationToPulledTableIndex(r3aPeriod, start);
                int periodElements = tablePeriodElements[index];

                r3aTools.add(new ArrayCompressionTool(1, tableIndex + 1, tableIndex + periodElements - 1));
                //matchingR3A.add(refCompressor.getMatchingR3A(i));
            }
        }
        return new ArrayCompressor(r3aTools);
    }

    protected static int iterationToPulledTableIndex(Period r3aPeriod, int iteration){
        //
        // get index <=> Inverse calculation of index compression
        // First approach : check the remainder == 1
        //
        // [3, 11, 26]
        // 1 4 7 12 15 18 23 27 30 33 38
        //
        // test input : 23
        // search period : period 11
        // 23 % 11 = 1, 23/11 = 2.xxx(3*2 elements)
        // 1 % 3 = 1, 1/3 = 0.xxx(1*0 elements) 
        // result = 3*2=6
        //
        //
        // test input : 30
        // search period : period 26
        // 30 % 26 = 4, 30/26 = 1.xxx(7*1 elements)
        // 4 % 3 = 1, 4/3 = 1.xxx(1 element)
        // result = 7*1+1=8
        //
        // test input : 29
        // search period : period 26
        // 29 % 26 = 3, 29/26 = 1.xxx(7*1 elements)
        // 3 % 3 = 0, 3/3 = 1.xxx(1 element)
        // result = -1 (last remainder is not one)
        // 
        // 

        if(iteration <= 0){
            return -1;
        }

        int[] tablePeriod = r3aPeriod.tablePeriod;
        int[] tablePeriodElements = r3aPeriod.tableElements;

        int index = 0;
        int remainder = iteration;
        
        for (int i = tablePeriod.length - 1; i >= 0; i--) {
            if(remainder < tablePeriod[i]){
                continue;
            }
            if(i < tablePeriod.length - 1 && remainder + tablePeriod[0] > tablePeriod[i + 1]){
                return -1;
            }
            

            index += remainder / tablePeriod[i] * tablePeriodElements[i];
            remainder %= tablePeriod[i];
        }
        return remainder == 1 ? index : -1;
    }

    protected static <R extends R3A> void safetyMatchTableSize(List<List<R>> table, int index){
        while(table.size() < index){
            table.add(null);
        }
        if(table.size() == index){
            table.add(new ArrayList<>());
        }
        if(index >= 0 && table.get(index) == null){
            table.set(index, new ArrayList<>());
        }
    }
    

    
    protected int iterationToCompTableIndex(int iteration){

        return switch(settings.r3aCompressionMethod()){
            case NO_COMPRESSION -> iteration;
            case LITTLE_COMPRESSION -> iterationToPulledTableIndex(r3aPeriod, iteration);
            case STRONGEST -> {
                int index = iterationToPulledTableIndex(r3aPeriod, iteration);
                yield index == -1 || pulledR3ACompressor == null ? -1 : pulledR3ACompressor.compress(index);
            }
        };

    }



    protected record Period(int[] tablePeriod, int[] requiredPerturbation, int[] tableElements) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Period(int[] t, int[] r, int[] te) &&
                Arrays.equals(tablePeriod, t) &&
                Arrays.equals(requiredPerturbation, r) &&
                Arrays.equals(tableElements, te);
        }
    
        @Override
        public String toString() {
            return "Period : " + Arrays.toString(tablePeriod)
                + "\nRequired Perturbation : " + Arrays.toString(requiredPerturbation)
                + "\nTable Elements : " + Arrays.toString(tableElements); 
        }
    
        @Override
        public int hashCode() {
            return Arrays.hashCode(tablePeriod) + Arrays.hashCode(requiredPerturbation) + Arrays.hashCode(tableElements);
        }

        private static int[] generatePeriodElements(int[] tablePeriod){
        
        
            // index compression : [3, 11, 26, 77] // index compression : [3, 11, 26, 77]
            // startIteration : 1  4  7 12 15 18 23 27
            // index :          0  1  2  3  4  5  6  7
            
            // 3 6 9 12 15 18 21 24 -> 3 6 9 -> 1 4 7 (11/3 = 3.xxx)
            // 11 22 33 44 55 66 -> 11, 22 -> 1 12 (26/11 = 2.xxx)
            // 26 52 78 104 130 -> 26, 52 -> 1 27 (77/26 = 2.xxx)
            // 
            // the remainder of [2]/[1] can also be divided by smaller period.
            // it can be recursive.
            //
            // period 11 : 11/3 =3.xxx (3 elements)                                                                              elements = 3 
            // period 26 : 26/11=2.xxx (3*2 elements), 26%11 = 4, 4/3 = 1.xxx (1 element)                                        elements = 3*2+1=7
            // period 77 : 77/26=2.xxx (7*2 elements), 77%26 = 25,25/11= 2.xxx (3*2 elements), 25%11=4, 4/3 = 1.xxx (1 element), elements = 7*2+3*2+1=21
            // Stored elements to memory
    
            int[] tablePeriodElements = new int[tablePeriod.length];
            for (int i = 0; i < tablePeriodElements.length; i++) {
                if(i == 0){
                    tablePeriodElements[i] = 1;
                    continue;
                }
                int elements = 0;
                int remainder = tablePeriod[i];
                for (int j = i - 1; j >= 0; j--) {
                    int groupAmount = remainder / tablePeriod[j];
                    remainder %= tablePeriod[j];
                    elements += groupAmount * tablePeriodElements[j];
                }
                tablePeriodElements[i] = elements;
            }
            return tablePeriodElements;
        }

        private static int getRequiredPerturbationCount(boolean isArtificialPeriod) {
            int required;
            if(isArtificialPeriod){
                required = 0;
                //artificially-created period's result iteration usually not a periodic point.
            }else{
                required = 2;
                //If the "period - 1" iterations are skipped, the resulting iteration is a periodic point.
                //That is, it is very small, which can cause floating-point errors, such as z + dz = 0 (e.g. big - big = small).
                //Therefore, Skip until a previous point of periodic point, In other words, skip "period - 2" iterations.
            }
            return required;
        }

        private static Temp generateTablePeriod(int[] referencePeriod, R3ASettings r3aSettings){

            // example 
            // period : [3, 11, 26]
            // 
            // -- : The space of R3A
            //
            // It : 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 ... longestPeriod
            //
            // 03 : 00 01 02 03 01 02 03 01 02 03 -- -- 01 02 03 01 02 03 01 02 03 -- -- 01 02 03 -- ...
            // 11 : 00 01 02 03 04 05 06 07 08 09 10 11 01 02 03 04 05 06 07 08 09 10 11 -- -- -- -- ...
            // 26 : 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 ...
            //
            //
            // Skips
            //  1 + 2 |  1 + 10 |  1 + 25
            //  4 + 2 | 12 + 10 | 27 + 25
            //  7 + 2 | 27 + 10
            // 12 + 2
            // 15 + 2
            // 18 + 2
            // 23 + 2
            // 27 + 2
            // ...
    
            int maxMultiplier = r3aSettings.maxMultiplierBetweenLevel();
            int minSkip = r3aSettings.minSkipReference();
            int longestPeriod = referencePeriod[referencePeriod.length - 1];
    
            int[] tablePeriodArrayTemp = new int[1];
            int[] requiredPerturbationArrayTemp = new int[1];
    
            int periodArraySize = 1;
            int currentRefPeriod = minSkip;
    
    
            tablePeriodArrayTemp[0] = currentRefPeriod;
            requiredPerturbationArrayTemp[0] = getRequiredPerturbationCount(Arrays.binarySearch(referencePeriod, currentRefPeriod) < 0);
            //first period is always minimum skip iteration when the longest period is larger than this,
            //and it is artificially-created period if generated period is not an element of generated period.
    
            for (int p : referencePeriod) {
    
                //Generate Period Array
    
                if(p >= minSkip && (p == longestPeriod && currentRefPeriod != longestPeriod || currentRefPeriod * maxMultiplier <= p)){
    
                    //If next valid period is "maxMultiplierBetweenLevel^2" times larger than "currentPeriod",
                    //add currentPeriod * "maxMultiplierBetweenLevel" period
                    //until the multiplier between level is lower than the square of "maxMultiplierBetweenLevel".
                    //It is artificially-created period.
    
                    while (currentRefPeriod >= minSkip && currentRefPeriod * maxMultiplier * maxMultiplier < p) {
                        if (periodArraySize == tablePeriodArrayTemp.length) {
                            tablePeriodArrayTemp = ArrayFunction.exp2xArr(tablePeriodArrayTemp);
                            requiredPerturbationArrayTemp = ArrayFunction.exp2xArr(requiredPerturbationArrayTemp);
                        }
                        tablePeriodArrayTemp[periodArraySize] = currentRefPeriod * maxMultiplier;
                        requiredPerturbationArrayTemp[periodArraySize] = getRequiredPerturbationCount(true);
    
                        periodArraySize++;
                        currentRefPeriod *= maxMultiplier;
                    }
    
                    //Otherwise, add generated period to period array.
    
                    if (periodArraySize == tablePeriodArrayTemp.length) {
                        tablePeriodArrayTemp = ArrayFunction.exp2xArr(tablePeriodArrayTemp);
                        requiredPerturbationArrayTemp = ArrayFunction.exp2xArr(requiredPerturbationArrayTemp);
                    }
                    tablePeriodArrayTemp[periodArraySize] = p;
                    requiredPerturbationArrayTemp[periodArraySize] = getRequiredPerturbationCount(false);
                    periodArraySize++;
                    currentRefPeriod = p;
    
    
                }
            }
            

            int[] tablePeriod = Arrays.copyOfRange(tablePeriodArrayTemp, 0, periodArraySize);
            int[] requiredPerturbation = Arrays.copyOfRange(requiredPerturbationArrayTemp, 0, periodArraySize);

            return new Temp(tablePeriod, requiredPerturbation);
        }

        private static Period create(int[] referencePeriod, R3ASettings r3aSettings){
            
            Temp temp = generateTablePeriod(referencePeriod, r3aSettings);
            int[] tablePeriod = temp.tablePeriod();
            int[] requiredPerturbation = temp.requiredPerturbation();
            int[] tablePeriodElements = generatePeriodElements(tablePeriod);

            return new Period(tablePeriod, requiredPerturbation, tablePeriodElements);
        }
        
        private record Temp(int[] tablePeriod, int[] requiredPerturbation){
            @Override
            public String toString(){
                return "";
            }

            @Override 
            public int hashCode(){
                return -1;
            }

            @Override
            public boolean equals(Object o){
                return o == this;
            }
        }

    }
}
