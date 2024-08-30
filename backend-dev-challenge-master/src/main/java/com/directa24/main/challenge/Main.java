package com.directa24.main.challenge;

import java.util.ArrayList;
import java.util.List;

public class Main {

   public static void main(String[] args) {
      List<String> directors = getDirectors(3);
      System.out.println(String.join("\n", directors));
   }

   public static List<String> getDirectors(int threshold) {
      String url = "https://directa24-movies.mocklab.io/api/movies/search?page=2";
      List<String> directors = new ArrayList<>();
      directors.add(url);
      return directors;
   }
}