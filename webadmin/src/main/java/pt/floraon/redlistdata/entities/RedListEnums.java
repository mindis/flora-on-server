package pt.floraon.redlistdata.entities;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by miguel on 16-11-2016.
 */
public class RedListEnums {

    public interface LabelledEnum {
        String getLabel();
    }

    public static <E extends Enum<E>> String[] getEnumValuesAsString(Class<E> clazz) {
        List<String> out = new ArrayList<String>();

        for(E v : EnumSet.allOf(clazz)) {
            out.add(v.toString());
        }
        return out.toArray(new String[out.size()]);
    }

    public static <E extends Enum<E> & LabelledEnum> String[] getEnumLabelsAsString(Class<E> clazz) {
        List<String> out = new ArrayList<String>();

        for(E v : EnumSet.allOf(clazz)) {
            out.add(v.getLabel());
        }
        return out.toArray(new String[out.size()]);
    }

    public enum DeclineDistribution implements LabelledEnum {
        NO_INFORMATION("No information")
        , STABLE("Stable")
        , POSSIBLE_INCREASE("Poss inc")
        , DECREASE_REVERSIBLE("Dec rev")
        , DECREASE_IRREVERSIBLE("Dec irrev")
        , POSSIBLE_DECREASE_FUTURE("Dec fut")
        , DECREASE_PAST_FUTURE("Dec past fut");

        private String label;

        DeclineDistribution(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum TypeOfPopulationEstimate implements LabelledEnum {
        EXACT_COUNT("Exact count")
        , APPROXIMATE_COUNT("Approximate count")
        , ROUGH_ESTIMATE("Rough estimate");

        private String label;

        TypeOfPopulationEstimate(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum NrMatureIndividuals implements LabelledEnum {
        NO_DATA("No data")
        , GT_10000("> 10000")
        , BET_2500_10000("2500 - 10000")
        , BET_250_2500("250 - 2500")
        , LT_250("< 250")
        , EXACT_NUMBER("Exact number");

        private String label;

        NrMatureIndividuals(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }

    }

    public enum DeclinePopulation implements LabelledEnum {
        NO_INFORMATION("No information")
        , STABLE("Stable")
        , DECREASE_REVERSIBLE("Dec rever")
        , DECREASE_IRREVERSIBLE("Dec irrev")
        , POSSIBLE_DECREASE_FUTURE("Poss dec")
        , DECREASE_PAST_FUTURE("Dec Past fut");

        private String label;

        DeclinePopulation(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum SeverelyFragmented implements LabelledEnum {
        NO_INFORMATION("No information")
        , SEVERELY_FRAGMENTED("Severely fragmented")
        , NOT_SEVERELY_FRAGMENTED("Not severely fragmented");

        private String label;

        SeverelyFragmented(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ExtremeFluctuations implements LabelledEnum {
        NOT_APPLICABLE("Not applicable")
        , NO("No")
        , YES("Yes");

        private String label;

        ExtremeFluctuations(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum GenerationLength implements LabelledEnum {
        NO_DATA("No data")
        , ONE_YEAR("1 year")
        , GT_ONE_YEAR("> 1 years");

        private String label;

        GenerationLength(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum HabitatTypes implements LabelledEnum {
        HAB_A("Habitat A")
        ,HAB_B("Habitat B")
        ,HAB_C("Habitat C");

        private String label;

        HabitatTypes(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum Uses implements LabelledEnum {
        USE_A("Medicinal")
        , USE_B("Edible")
        , USE_C("Ornamental");

        private String label;

        Uses(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum Overexploitation implements LabelledEnum {
        NO_DATA("No information")
        , OVEREXPLOITED("Potentially threatened by overexploitation")
        , EXPLOITED("Exploited but not overexploited")
        , NOT_EXPLOITED("Not exploited");

        private String label;

        Overexploitation(String desc) {
            this.label = desc;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

}