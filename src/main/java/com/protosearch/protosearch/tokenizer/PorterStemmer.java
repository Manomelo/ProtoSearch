package com.protosearch.protosearch.tokenizer;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PorterStemmer {


    /**
     * Aplica o algoritmo de stemming a uma palavra.
     *
     * @param word A palavra a ser reduzida à sua raiz.
     * @return A raiz da palavra após o stemming.
     */
    public String stem(String word) {
        if (word.length() <= 2) return word;

        word = step1a(word);
        word = step1b(word);
        word = step1c(word);
        word = step2(word);
        word = step3(word);
        word = step4(word);
        word = step5a(word);
        word = step5b(word);

        return word;
    }

    /**
     * Aplica o algoritmo de stemming a uma lista de palavras.
     *
     * @return Uma lista de palavras após o stemming.
     */
    public List<String> stemAll(List<String> tokens) {
        return tokens.stream()
                .map(this::stem)
                .toList();
    }


    /**
     * Conta o número de sequências vogal-consoante (VC) em um stem.
     *
     * @param stem O stem a ser analisado.
     * @return O número de sequências VC.
     */
    private int measure(String stem) {
        int m = 0;
        boolean prevWasVowel = false;
        for (char c : stem.toCharArray()) {
            if (isVowel(c, stem)) {
                prevWasVowel = true;
            } else {
                if (prevWasVowel) m++;
                prevWasVowel = false;
            }
        }
        return m;
    }

    /**
     * Verifica se um caractere é uma vogal.
     *
     * @param c O caractere a ser verificado.
     * @param word A palavra onde o caractere está localizado.
     * @return True se o caractere for uma vogal, caso contrário false.
     */
    private boolean isVowel(char c, String word) {
        return "aeiou".indexOf(c) >= 0 ||
                (c == 'y' && word.indexOf(c) > 0);
    }

    /**
     * Verifica se um stem contém pelo menos uma vogal.
     *
     * @param stem O stem a ser analisado.
     * @return True se o stem contiver uma vogal, caso contrário false.
     */
    private boolean containsVowel(String stem) {
        for (char c : stem.toCharArray())
            if (isVowel(c, stem)) return true;
        return false;
    }

    /**
     * Verifica se uma palavra termina com uma consoante dupla.
     *
     * @param word A palavra a ser analisada.
     * @return True se a palavra terminar com uma consoante dupla, caso contrário false.
     */
    private boolean endsWithDoubleConsonant(String word) {
        int len = word.length();
        return len >= 2
                && word.charAt(len - 1) == word.charAt(len - 2)
                && !isVowel(word.charAt(len - 1), word);
    }

    /**
     * Aplica as regras da etapa 1a do algoritmo Porter Stemmer.
     * Trata plurais e terminações simples.
     *
     * @param word A palavra a ser transformada.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String step1a(String word) {
        if (word.endsWith("sses")) return word.substring(0, word.length() - 2);
        if (word.endsWith("ies"))  return word.substring(0, word.length() - 2);
        if (word.endsWith("ss"))   return word;
        if (word.endsWith("s"))    return word.substring(0, word.length() - 1);
        return word;
    }


    /**
     * Aplica as regras da etapa 1b do algoritmo Porter Stemmer.
     * Trata tempos verbais no passado e gerúndios.
     *
     * @param word A palavra a ser transformada.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String step1b(String word) {
        if (word.endsWith("eed")) {
            String stem = word.substring(0, word.length() - 3);
            return measure(stem) > 0 ? stem + "ee" : word;
        }
        if (word.endsWith("ed")) {
            String stem = word.substring(0, word.length() - 2);
            if (containsVowel(stem)) return fixStep1b(stem);
        }
        if (word.endsWith("ing")) {
            String stem = word.substring(0, word.length() - 3);
            if (containsVowel(stem)) return fixStep1b(stem);
        }
        return word;
    }

    /**
     * Aplica correções adicionais na etapa 1b.
     *
     * @param stem O stem a ser corrigido.
     * @return O stem corrigido.
     */
    private String fixStep1b(String stem) {
        if (stem.endsWith("at") || stem.endsWith("bl") || stem.endsWith("iz"))
            return stem + "e";
        if (endsWithDoubleConsonant(stem) && !stem.endsWith("l")
                && !stem.endsWith("s") && !stem.endsWith("z"))
            return stem.substring(0, stem.length() - 1);
        return stem;
    }


    /**
     * Aplica as regras da etapa 1c do algoritmo Porter Stemmer.
     * Substitui 'y' por 'i' em palavras apropriadas.
     *
     * @param word A palavra a ser transformada.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String step1c(String word) {
        if (word.endsWith("y")) {
            String stem = word.substring(0, word.length() - 1);
            if (containsVowel(stem)) return stem + "i";
        }
        return word;
    }


    /**
     * Aplica as regras da etapa 2 do algoritmo Porter Stemmer.
     * Reduz sufixos comuns a formas mais simples.
     *
     * @param word A palavra a ser transformada.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String step2(String word) {
        String[][] rules = {
                {"ational", "ate"}, {"tional", "tion"}, {"enci", "ence"},
                {"anci", "ance"}, {"izer", "ize"},      {"abli", "able"},
                {"alli", "al"},   {"entli", "ent"},      {"eli", "e"},
                {"ousli", "ous"}, {"ization", "ize"},    {"ation", "ate"},
                {"ator", "ate"},  {"alism", "al"},        {"iveness", "ive"},
                {"fulness", "ful"}, {"ousness", "ous"},  {"aliti", "al"},
                {"iviti", "ive"}, {"biliti", "ble"}
        };
        return applyRules(word, rules, 0);
    }


    /**
     * Aplica as regras da etapa 3 do algoritmo Porter Stemmer.
     * Remove sufixos adicionais.
     *
     * @param word A palavra a ser transformada.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String step3(String word) {
        String[][] rules = {
                {"icate", "ic"}, {"ative", ""}, {"alize", "al"},
                {"iciti", "ic"}, {"ical", "ic"}, {"ful", ""}, {"ness", ""}
        };
        return applyRules(word, rules, 0);
    }


    /**
     * Aplica as regras da etapa 4 do algoritmo Porter Stemmer.
     * Remove sufixos residuais.
     *
     * @param word A palavra a ser transformada.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String step4(String word) {
        String[] suffixes = {
                "al", "ance", "ence", "er", "ic", "able", "ible",
                "ant", "ement", "ment", "ent", "ion", "ou", "ism",
                "ate", "iti", "ous", "ive", "ize"
        };
        for (String suffix : suffixes) {
            if (word.endsWith(suffix)) {
                String stem = word.substring(0, word.length() - suffix.length());
                // Special case for "ion"
                if (suffix.equals("ion")) {
                    if (measure(stem) > 1 && (stem.endsWith("s") || stem.endsWith("t")))
                        return stem;
                } else if (measure(stem) > 1) {
                    return stem;
                }
            }
        }
        return word;
    }


    /**
     * Aplica as regras da etapa 5a do algoritmo Porter Stemmer.
     * @param word A palavra a ser transformada.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String step5a(String word) {
        if (word.endsWith("e")) {
            String stem = word.substring(0, word.length() - 1);
            if (measure(stem) > 1) return stem;
            if (measure(stem) == 1 && !endsWithDoubleConsonant(stem)) return stem;
        }
        return word;
    }


    /**
     * Aplica as regras da etapa 5b do algoritmo Porter Stemmer.
     * Reduz consoantes duplas finais.
     *
     * @param word A palavra a ser transformada.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String step5b(String word) {
        if (measure(word) > 1 && endsWithDoubleConsonant(word) && word.endsWith("l"))
            return word.substring(0, word.length() - 1);
        return word;
    }

    /**
     * Aplica as regras de transformação de sufixos a uma palavra.
     *
     * @param word A palavra a ser transformada.
     * @param rules As regras de transformação (sufixo e substituição).
     * @param minMeasure O número mínimo de sequências VC necessário para aplicar a regra.
     * @return A palavra transformada após a aplicação das regras.
     */
    private String applyRules(String word, String[][] rules, int minMeasure) {
        for (String[] rule : rules) {
            if (word.endsWith(rule[0])) {
                String stem = word.substring(0, word.length() - rule[0].length());
                if (measure(stem) > minMeasure) return stem + rule[1];
            }
        }
        return word;
    }
}
