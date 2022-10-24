import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MovieAnalyzer{
    public static class Movie
    {
        private String Poster_Link;
        private String Series_Title;
        private int Released_Year;
        private String Certificate;
        private int Runtime;
        private String Genre;

        private List<String> GenreList;
        private float IMDB_Rating;
        private String Overview;
        private int Meta_score;
        private String Director;
        private String Star1;
        private String Star2;
        private String Star3;
        private String Star4;

        private String[] StarArray;
        private int Noofvotes;
        private int Gross;

        private int OverviewLength;

        public Movie(String poster, String title, int year, String ct, int rt, String genre, float rating, String overview,
                     int score, String director, String star1, String star2, String star3, String star4, int votes,int gross) {
            Poster_Link = poster;
            Series_Title = title;
            Released_Year = year;
            Certificate = ct;
            Runtime = rt;
            Genre = genre;
            IMDB_Rating = rating;
            Overview = overview;
            Meta_score = score;
            Director = director;
            Star1 = star1;
            Star2 = star2;
            Star3 = star3;
            Star4 = star4;
            Noofvotes = votes;
            Gross = gross;
        }

        public String toString()
        {
            return this.Series_Title + '\n' + this.Released_Year + '\n' + this.Runtime + '\n' + this.Genre + '\n' +
                    this.IMDB_Rating + '\n' + this.Overview + '\n' + this.Gross + '\n';
        }

        public String getSeries_Title() {return Series_Title;}

        public int getReleased_Year() {return Released_Year;}

        public int getRuntime() {return Runtime;}

        public String getGenre() {return Genre;}

        public double getIMDB_Rating() {return IMDB_Rating;}

        public String getOverview() {return Overview;}

        public int getGross() {return Gross;}

        public String getStar1() {return Star1;}

        public String getStar2() {return Star2;}

        public String getStar3() {return Star3;}

        public String getStar4() {return Star4;}

        public void setOverviewLength()
        {
            this.OverviewLength = this.Overview.length();
        }
        public int getOverviewLength()
        {
            return Overview.length();
        }
        public void setGenreList()
        {
            this.GenreList = new ArrayList<>();
            String cur = this.Genre;
            int endpos = 0;
            for(int i = 0;i < cur.length();i++)
            {
                if(cur.charAt(i) == ',')
                {
                    this.GenreList.add(cur.substring(endpos, i));
                    endpos = i + 2;
                }
            }
            this.GenreList.add(cur.substring(endpos));
        }
        public List<String> getGenreList()
        {
            return GenreList;
        }

        public void setStarArray()
        {
            this.StarArray = new String[4];
            this.StarArray[0] = this.Star1;
            this.StarArray[1] = this.Star2;
            this.StarArray[2] = this.Star3;
            this.StarArray[3] = this.Star4;
        }

        public String[] getStarArray()
        {
            return StarArray;
        }
    }

    public static class SubMovie
    {
        private String star;
        private double rating;
        private int gross;

        public SubMovie(String star, double rating, int gross)
        {
            this.star = star;
            this.rating = rating;
            this.gross = gross;
        }

        String getStar() {return this.star;}
        double getRating() {return this.rating;}
        int getGross() {return this.gross;}
    }
//    private Stream<Movie> movies;

    private List<Movie> movieList = new ArrayList<>();

    public int changeString(String str)
    {
        int k = 0,op = 1;
        for(int i = 0;i < str.length();i++)
        {
            char c = str.charAt(i);
            if(c >= '0' && c <= '9')
            {
                k = k * 10;
                k += c - '0';
            }
            if(c == '-') op = -1;
        }
        return k * op;
    }

    public String deleteBracket(String overview)
    {
        if(overview.charAt(0) == '"') overview = overview.substring(1, overview.length() - 1);
        return overview;
    }

    public String nullCheck(String s, int index)
    {
        if(s.length() == 0)
        {
            switch (index) {
                case 2, 4, 8, 14, 15 -> s = "-1";
                case 6 -> s = "-1.0";
                default -> s = "Null";
            }
        }
        return s;
    }

    public MovieAnalyzer(String Dataset_path)
    {
//        List<Movie> movieList = new ArrayList<>();
        try
        {
            FileInputStream fis = new FileInputStream(Dataset_path);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String currentLine = null;
            boolean firstRead = true;
            while((currentLine = br.readLine()) != null)
            {
                if(firstRead)
                {
                    //omit the first line
                    firstRead = false;
                    continue;
                }
                int[] split = new int[20];
                String[] sub = new String[20];
                int count = 0;
                boolean flag = false;
                for(int i = 0;i < currentLine.length();i++)
                {
                    //System.out.println(currentLine.charAt(i));
                    if(!flag && currentLine.charAt(i) == '"') flag = true;
                    else if(flag && currentLine.charAt(i) == '"') flag = false;
                    if(!flag && currentLine.charAt(i) == ',') split[count++] = i;
                }

                for(int i = 0;i <= count;i++)
                {
                    if(i == 0) sub[i] = currentLine.substring(0, split[i]);
                    else if(i == count) sub[i] = currentLine.substring(split[i-1] + 1, currentLine.length());
                    else sub[i] = currentLine.substring(split[i-1] + 1, split[i]);
                }

                for(int i = 0;i <= count;i++) sub[i] = nullCheck(sub[i], i);
//                for(int i = 0;i <= count;i++) System.out.println(sub[i]);

                Movie m = new Movie(sub[0],deleteBracket(sub[1]),Integer.parseInt(sub[2]),sub[3],changeString(sub[4]),
                        deleteBracket(sub[5]), Float.parseFloat(sub[6]),
                        deleteBracket(sub[7]),Integer.parseInt(sub[8]),sub[9],sub[10],sub[11],sub[12],sub[13],
                        Integer.parseInt(sub[14]),changeString(sub[15]));
                m.setGenreList();
                m.setStarArray();
//                System.out.println(m.getGenreList());
                movieList.add(m);
            }
        }
        catch (Exception e) {e.printStackTrace();}
//        movies = movieList.stream();
    }
    public void print()
    {
        Stream<Movie> movies = movieList.stream();
        movies.forEach(System.out::println);
    }
    ///

    //First Function
    public Map<Integer, Integer> getMovieCountByYear()
    {
        Stream<Movie> movies = movieList.stream();
        Map<Integer, Long> movieCountPerYear = movies.collect(Collectors.groupingBy(Movie::getReleased_Year,
                Collectors.counting()));
        List<Map.Entry<Integer, Long>> list = new ArrayList<>(movieCountPerYear.entrySet());
        Collections.sort(list, ((o1, o2) -> o2.getKey() - o1.getKey()));
        Map<Integer, Integer> newCount = new LinkedHashMap<>();
        for(Map.Entry<Integer, Long> movieEntry : list)
        {
            newCount.put(movieEntry.getKey(),movieEntry.getValue().intValue());
        }
        return newCount;
    }

    //Second Function
    public Map<String, Integer> getMovieCountByGenre()
    {
        List<List<String>> a = new ArrayList<>();
        for(Movie b : movieList) a.add(b.getGenreList());
        Stream<String> genreStream = a.stream().flatMap((childList) -> childList.stream());
        Map<String, Long> genreCount = genreStream.collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
//        List<Map.Entry<String, Long>> sortGenre = genreCount.entrySet().stream().
//                sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
        List<Map.Entry<String, Long>> list = new ArrayList<>(genreCount.entrySet());
        Collections.sort(list, (o1, o2) -> {
            if(Objects.equals(o1.getValue(), o2.getValue()))  return o1.getKey().compareTo(o2.getKey());
            else return (int) (o2.getValue() - o1.getValue());
        });
        Map<String, Integer> newCount = new LinkedHashMap<>();
        for(Map.Entry<String, Long> genreEntry : list)
        {
            if(genreEntry.getKey().equals("Null")) continue; //Ignore null data.
            newCount.put(genreEntry.getKey(), genreEntry.getValue().intValue());
        }
        return newCount;
    }

    //Third Function
    public Map<List<String>, Integer> getCoStarCount()
    {
        Map<List<String>, Integer> coStarCount = new LinkedHashMap<>();
        for(Movie curMovie : movieList)
        {
            for(int i = 0; i < 3; i++)
            {
                if(curMovie.getStarArray()[i].equals("Null")) continue;
                for(int j = i + 1; j <= 3; j++)
                {
                    if(curMovie.getStarArray()[j].equals("Null")) continue;
                    List<String> cur = Arrays.asList(curMovie.getStarArray()[i],curMovie.getStarArray()[j]);
                    List<String> cur1 = Arrays.asList(curMovie.getStarArray()[j],curMovie.getStarArray()[i]);
                    if(coStarCount.containsKey(cur))
                    {
                        int k = coStarCount.get(cur);
                        coStarCount.put(cur, k+1);
                    }
                    else if(coStarCount.containsKey(cur1))
                    {
                        int k = coStarCount.get(cur1);
                        coStarCount.put(cur1, k+1);
                    }
                    else
                    {
                        if(curMovie.getStarArray()[i].compareTo(curMovie.getStarArray()[j]) < 0) coStarCount.put(cur, 1);
                        else coStarCount.put(cur1, 1);
                    }
                }
            }
        }
        List<Map.Entry<List<String>, Integer>> list = new ArrayList<>(coStarCount.entrySet());
        Collections.sort(list, ((o1, o2) -> o2.getValue() - o1.getValue()));
        Map<List<String>, Integer> newCount = new LinkedHashMap<>();
//        int count = 0;
        for(Map.Entry<List<String>, Integer> coStarEntry : list)
        {
//            count++;
            newCount.put(coStarEntry.getKey(),coStarEntry.getValue());
        }
//        System.out.println(count);
        return newCount;
    }

    //Fourth Function
    public List<String> getTopMovies(int top_k, String by)
    {
        Stream<Movie> movies = movieList.stream();
        List<String> topMovies = new ArrayList<>();
        if(by.equals("runtime"))
        {
            List<Movie> a = movies.sorted(Comparator.comparing(Movie::getRuntime).reversed()
                    .thenComparing(Movie::getSeries_Title)).collect(Collectors.toList());
            int count = 0;
            for(Movie b : a)
            {
                topMovies.add(b.getSeries_Title());
                count++;
                if(count == top_k) break;
            }
        }
        if(by.equals("overview"))
        {
            List<Movie> a = movies.sorted(Comparator.comparing(Movie::getOverviewLength).reversed()
                    .thenComparing(Movie::getSeries_Title)).collect(Collectors.toList());
            int count = 0;
            for(Movie b : a)
            {
                topMovies.add(b.getSeries_Title());
                count++;
                if(count == top_k) break;
            }
        }
        return topMovies;
    }

    //Fifth Function
    public List<String> getTopStars(int top_k, String by)
    {
        List<String> answerList = new ArrayList<>();
        List<SubMovie> subMoviesList = new ArrayList<>();
        List<SubMovie> aveMoviesList = new ArrayList<>();
        for(Movie cur : movieList)
        {
            for(int i = 0;i <= 3;i++)
            {
                subMoviesList.add(new SubMovie(cur.getStarArray()[i], cur.getIMDB_Rating(), cur.getGross()));
            }
        }
        Map<String, Double> answerMap = new HashMap<>();
        if(by.equals("rating"))
        {
            Stream<SubMovie> curStream = subMoviesList.stream().filter(a -> a.getRating() > 0);
             answerMap = curStream.collect(Collectors.groupingBy(SubMovie::getStar,
                    Collectors.averagingDouble(SubMovie::getRating)));
        }
        if(by.equals("gross")) {
            Stream<SubMovie> curStream = subMoviesList.stream().filter(a -> a.getGross() > 0);
            answerMap = curStream.collect(Collectors.groupingBy(SubMovie::getStar,
                    Collectors.averagingDouble(SubMovie::getGross)));
        }
        for(Map.Entry<String, Double> a : answerMap.entrySet())
        {
            aveMoviesList.add(new SubMovie(a.getKey(),a.getValue(),1));
        }
        List<SubMovie> sortedMoviesList = aveMoviesList.stream().sorted(Comparator.comparing(SubMovie::getRating).
                reversed().thenComparing(SubMovie::getStar)).collect(Collectors.toList());
        int count = 0;
        for(SubMovie cur : sortedMoviesList)
        {
            answerList.add(cur.getStar());
            count++;
//            System.out.println(cur.getStar());
//            System.out.println(cur.getRating());
            if(count == top_k) break;
        }
        return answerList;
    }
    //Sixth Function
    public List<String> searchMovies(String genre, float min_rating, int max_runtime)
    {
        List<String> answerList = new ArrayList<>();
        List<Movie> answerMovieList = movieList.stream().filter(a->a.getRuntime() <= max_runtime && a.getIMDB_Rating() >= min_rating
            && a.getGenre().contains(genre)).sorted(Comparator.comparing(Movie::getSeries_Title)).collect(Collectors.toList());
        for(Movie a : answerMovieList)
        {
            answerList.add(a.getSeries_Title());
        }
        return answerList;
    }
}