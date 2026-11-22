import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MovieAnalyzer {
    private record Movie(String title, int year, int runtime, String genre,
                         float rating, String overview, int gross, List<String> stars) {
        @Override
        public String toString() {
            return title + "\n" + year + "\n" + runtime + "\n" + genre + "\n"
                    + rating + "\n" + overview + "\n" + gross + "\n";
        }
    }

    private final List<Movie> movies = new ArrayList<>();

    public MovieAnalyzer(String datasetPath) {
        try (BufferedReader reader = Files.newBufferedReader(
                Path.of(datasetPath), StandardCharsets.UTF_8)) {
            readRecord(reader); // Skip the CSV header.
            String row;
            while ((row = readRecord(reader)) != null) {
                List<String> f = parseCsv(row);
                if (f.size() != 16) {
                    throw new IllegalArgumentException("Expected 16 CSV columns, got " + f.size());
                }
                movies.add(new Movie(text(f, 1), number(f.get(2)), number(f.get(4)),
                        text(f, 5), f.get(6).isEmpty() ? -1f : Float.parseFloat(f.get(6)),
                        text(f, 7), number(f.get(15)),
                        List.of(text(f, 10), text(f, 11), text(f, 12), text(f, 13))));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read movie dataset", e);
        }
    }

    private static String text(List<String> fields, int index) {
        return fields.get(index).isEmpty() ? "Null" : fields.get(index);
    }

    private static int number(String value) {
        if (value.isEmpty()) return -1;
        return Integer.parseInt(value.replaceAll("[^0-9-]", ""));
    }

    // Read one logical CSV record, including embedded newlines in quoted fields.
    private static String readRecord(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) return null;
        StringBuilder record = new StringBuilder(line);
        while (record.chars().filter(c -> c == '"').count() % 2 != 0) {
            line = reader.readLine();
            if (line == null) throw new IllegalArgumentException("Unclosed CSV quote");
            record.append('\n').append(line);
        }
        return record.toString();
    }

    private static List<String> parseCsv(String row) {
        List<String> fields = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < row.length(); i++) {
            char c = row.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < row.length() && row.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                fields.add(value.toString());
                value.setLength(0);
            } else {
                value.append(c);
            }
        }
        fields.add(value.toString());
        return fields;
    }

    private static <K> Map<K, Integer> sortCounts(
            Map<K, Integer> counts, Comparator<Map.Entry<K, Integer>> order) {
        return counts.entrySet().stream().sorted(order).collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public void print() {
        movies.forEach(System.out::println);
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        Map<Integer, Integer> counts = new HashMap<>();
        movies.forEach(m -> counts.merge(m.year(), 1, Integer::sum));
        return sortCounts(counts, Map.Entry.<Integer, Integer>comparingByKey().reversed());
    }

    public Map<String, Integer> getMovieCountByGenre() {
        Map<String, Integer> counts = new HashMap<>();
        for (Movie m : movies) {
            for (String genre : m.genre().split(",\\s*")) {
                genre = genre.trim();
                if (!genre.equals("Null")) counts.merge(genre, 1, Integer::sum);
            }
        }
        return sortCounts(counts, Comparator.<Map.Entry<String, Integer>>
                comparingInt(Map.Entry::getValue).reversed().thenComparing(Map.Entry::getKey));
    }

    public Map<List<String>, Integer> getCoStarCount() {
        Map<List<String>, Integer> counts = new LinkedHashMap<>();
        for (Movie m : movies) {
            List<String> stars = m.stars();
            for (int i = 0; i < stars.size(); i++) {
                for (int j = i + 1; j < stars.size(); j++) {
                    String a = stars.get(i), b = stars.get(j);
                    if (a.equals("Null") || b.equals("Null")) continue;
                    List<String> pair = a.compareTo(b) <= 0 ? List.of(a, b) : List.of(b, a);
                    counts.merge(pair, 1, Integer::sum);
                }
            }
        }
        return sortCounts(counts, Map.Entry.<List<String>, Integer>comparingByValue().reversed());
    }

    public List<String> getTopMovies(int top_k, String by) {
        Comparator<Movie> order = switch (by) {
            case "runtime" -> Comparator.comparingInt(Movie::runtime);
            case "overview" -> Comparator.comparingInt(m -> m.overview().length());
            default -> null;
        };
        if (order == null || top_k <= 0) return List.of();
        return movies.stream().sorted(order.reversed().thenComparing(Movie::title))
                .limit(top_k).map(Movie::title).toList();
    }

    public List<String> getTopStars(int top_k, String by) {
        if (top_k <= 0 || (!by.equals("rating") && !by.equals("gross"))) return List.of();
        Map<String, DoubleSummaryStatistics> stats = new HashMap<>();
        for (Movie m : movies) {
            double value = by.equals("rating") ? m.rating() : m.gross();
            if (value <= 0) continue;
            for (String star : m.stars()) {
                if (!star.equals("Null")) {
                    stats.computeIfAbsent(star, k -> new DoubleSummaryStatistics()).accept(value);
                }
            }
        }
        return stats.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, DoubleSummaryStatistics>>
                        comparingDouble(e -> e.getValue().getAverage()).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(top_k).map(Map.Entry::getKey).toList();
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        return movies.stream()
                .filter(m -> m.runtime() <= max_runtime && m.rating() >= min_rating)
                .filter(m -> Arrays.asList(m.genre().split(",\\s*")).contains(genre))
                .sorted(Comparator.comparing(Movie::title))
                .map(Movie::title).toList();
    }
}
