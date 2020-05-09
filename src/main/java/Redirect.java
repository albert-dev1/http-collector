import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Redirect {
    private String source;
    private String location;
    private int type;
}
