package kr.merutilm.rff.approx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.merutilm.rff.formula.MandelbrotReference;
import kr.merutilm.rff.functions.ArrayCompressionTool;
import kr.merutilm.rff.functions.ArrayCompressor;
import kr.merutilm.rff.settings.MPACompressionMethod;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.util.ArrayFunction;

public abstract class MPATable {

    protected final MPASettings settings;
    protected final ArrayCompressor pulledMPACompressor;
    protected final Period mpaPeriod;

    protected static final int REQUIRED_PERTURBATION = 2;

    protected MPATable(MandelbrotReference reference, MPASettings mpaSettings) {
        long[] referencePeriod = reference.period();
        long longestPeriod = reference.longestPeriod();
        int minSkip = mpaSettings.minSkipReference();

        if (longestPeriod < minSkip) {
            this.settings = mpaSettings;
            this.mpaPeriod = null;
            this.pulledMPACompressor = ArrayCompressor.EMPTY_COMPRESSOR;
            return;
        }

        MPACompressionMethod compressionMethod = mpaSettings.mpaCompressionMethod();
        Period mpaPeriod = MPATable.Period.create(referencePeriod, mpaSettings);
        ArrayCompressor pulledMPACompressor = compressionMethod == MPACompressionMethod.STRONGEST ? createPulledMPACompressor(mpaPeriod, reference.referenceCompressor()) : ArrayCompressor.EMPTY_COMPRESSOR;

        this.settings = mpaSettings;
        this.mpaPeriod = mpaPeriod;
        this.pulledMPACompressor = pulledMPACompressor;

    }

    public abstract int length();

    private static ArrayCompressor createPulledMPACompressor(Period mpaPeriod, ArrayCompressor refCompressor) {

        List<ArrayCompressionTool> refCompTools = refCompressor.tools();
        List<ArrayCompressionTool> mpaTools = new ArrayList<>();
        long[] tablePeriod = mpaPeriod.tablePeriod;
        long[] tablePeriodElements = mpaPeriod.tableElements;
        boolean[] isArtificial = mpaPeriod.isArtificial;

        for (ArrayCompressionTool tool : refCompTools) {
            long start = tool.start();
            long length = tool.range();
            int index = Arrays.binarySearch(tablePeriod, length + 1);
            long tableIndex = iterationToPulledTableIndex(mpaPeriod, start);

            // Check if the reference compressor is same as period.
            // However, The Computer doesn't know whether the compressor's length came from skipping to the periodic point, or being cut off in the middle.
            // So, Do check tableIndex too.
            if (index >= 0 && tableIndex >= 0 && !isArtificial[index]) {
                long periodElements = tablePeriodElements[index];
                mpaTools.add(new ArrayCompressionTool(1, tableIndex + 1, tableIndex + periodElements - 1));
            }
        }
        return new ArrayCompressor(mpaTools);
    }

    protected static long iterationToPulledTableIndex(Period mpaPeriod, long iteration) {
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
        // 

        if (iteration <= 0) {
            return -1;
        }

        long[] tablePeriod = mpaPeriod.tablePeriod;
        long[] tablePeriodElements = mpaPeriod.tableElements;

        long index = 0;
        long remainder = iteration;

        for (int i = tablePeriod.length - 1; i >= 0; i--) {
            if (remainder < tablePeriod[i]) {
                continue;
            }
            if (i < tablePeriod.length - 1 && remainder + tablePeriod[0] - REQUIRED_PERTURBATION + 1 > tablePeriod[i + 1]) {
                return -1; //Insufficient length, ("Pulled Table Index" must be skipped for at least "shortest period")
            }


            index += remainder / tablePeriod[i] * tablePeriodElements[i];
            remainder %= tablePeriod[i];
        }
        return remainder == 1 ? index : -1;
    }

    protected static <R extends PA> void safetyMatchTableSize(List<List<R>> table, int index) {
        while (table.size() < index) {
            table.add(null);
        }
        if (table.size() == index) {
            table.add(new ArrayList<>());
        }
        if (index >= 0 && table.get(index) == null) {
            table.set(index, new ArrayList<>());
        }
    }


    protected int iterationToCompTableIndex(long iteration) {

        return switch (settings.mpaCompressionMethod()) {
            case NO_COMPRESSION -> (int)iteration;
            case LITTLE_COMPRESSION -> (int)iterationToPulledTableIndex(mpaPeriod, iteration);
            case STRONGEST -> {
                long index = iterationToPulledTableIndex(mpaPeriod, iteration);
                yield index == -1 ? -1 : pulledMPACompressor.compress(index);
            }
        };

    }



    protected record Period(long[] tablePeriod, boolean[] isArtificial, long[] tableElements) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Period(long[] t, boolean[] a, long[] te) &&
                   Arrays.equals(tablePeriod, t) &&
                   Arrays.equals(isArtificial, a) &&
                   Arrays.equals(tableElements, te);
        }

        @Override
        public String toString() {
            return "Period : " + Arrays.toString(tablePeriod)
                   + "\nIsArtificial : " + Arrays.toString(isArtificial)
                   + "\nTable Elements : " + Arrays.toString(tableElements);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(tablePeriod) + Arrays.hashCode(tableElements);
        }

        private static long[] generatePeriodElements(long[] tablePeriod) {


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

            long[] tablePeriodElements = new long[tablePeriod.length];
            for (int i = 0; i < tablePeriodElements.length; i++) {
                if (i == 0) {
                    tablePeriodElements[i] = 1;
                    continue;
                }
                long elements = 0;
                long remainder = tablePeriod[i];
                for (int j = i - 1; j >= 0; j--) {
                    long groupAmount = remainder / tablePeriod[j];
                    remainder %= tablePeriod[j];
                    elements += groupAmount * tablePeriodElements[j];
                }
                tablePeriodElements[i] = elements;
            }
            return tablePeriodElements;
        }

        private static Temp generateTablePeriod(long[] referencePeriod, MPASettings MPASettings) {

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

            int maxMultiplier = MPASettings.maxMultiplierBetweenLevel();
            int minSkip = MPASettings.minSkipReference();
            long longestPeriod = referencePeriod[referencePeriod.length - 1];

            long[] tablePeriodArrayTemp = new long[1];
            boolean[] isArtificialArrayTemp = new boolean[1];

            int periodArraySize = 1;
            long currentRefPeriod = minSkip;


            tablePeriodArrayTemp[0] = currentRefPeriod;
            isArtificialArrayTemp[0] = Arrays.binarySearch(referencePeriod, currentRefPeriod) < 0;
            //first period is always minimum skip iteration when the longest period is larger than this,
            //and it is artificially-created period if generated period is not an element of generated period.

            for (long p : referencePeriod) {

                //Generate Period Array

                if (p >= minSkip && (p == longestPeriod && currentRefPeriod != longestPeriod || currentRefPeriod * maxMultiplier <= p)) {

                    //If next valid period is "maxMultiplierBetweenLevel^2" times larger than "currentPeriod",
                    //add currentPeriod * "maxMultiplierBetweenLevel" period
                    //until the multiplier between level is lower than the square of "maxMultiplierBetweenLevel".
                    //It is artificially-created period.

                    while (currentRefPeriod >= minSkip && currentRefPeriod * maxMultiplier * maxMultiplier < p) {
                        if (periodArraySize == tablePeriodArrayTemp.length) {
                            tablePeriodArrayTemp = ArrayFunction.exp2xArr(tablePeriodArrayTemp);
                            isArtificialArrayTemp = ArrayFunction.exp2xArr(isArtificialArrayTemp);
                        }
                        tablePeriodArrayTemp[periodArraySize] = currentRefPeriod * maxMultiplier;
                        isArtificialArrayTemp[periodArraySize] = true;

                        periodArraySize++;
                        currentRefPeriod *= maxMultiplier;
                    }

                    //Otherwise, add generated period to period array.

                    if (periodArraySize == tablePeriodArrayTemp.length) {
                        tablePeriodArrayTemp = ArrayFunction.exp2xArr(tablePeriodArrayTemp);
                        isArtificialArrayTemp = ArrayFunction.exp2xArr(isArtificialArrayTemp);
                    }
                    tablePeriodArrayTemp[periodArraySize] = p;
                    isArtificialArrayTemp[periodArraySize] = false;
                    periodArraySize++;
                    currentRefPeriod = p;


                }
            }


            long[] tablePeriod = Arrays.copyOfRange(tablePeriodArrayTemp, 0, periodArraySize);
            boolean[] isArtificial = Arrays.copyOfRange(isArtificialArrayTemp, 0, periodArraySize);
            return new Temp(tablePeriod, isArtificial);
        }

        private static Period create(long[] referencePeriod, MPASettings mpaSettings) {

            Temp temp = generateTablePeriod(referencePeriod, mpaSettings);
            long[] tablePeriod = temp.tablePeriod();
            boolean[] isArtificial = temp.isArtificial();
            long[] tablePeriodElements = generatePeriodElements(tablePeriod);

            return new Period(tablePeriod, isArtificial, tablePeriodElements);
        }

        private record Temp(long[] tablePeriod, boolean[] isArtificial) {
            @Override
            public String toString() {
                return "";
            }

            @Override
            public int hashCode() {
                return -1;
            }

            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        }

    }
}
