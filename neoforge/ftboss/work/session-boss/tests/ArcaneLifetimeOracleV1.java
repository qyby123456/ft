import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
public final class ArcaneLifetimeOracleV1 {
    public static void main(String[] arguments)throws Exception{
        List<Map<String,Object>> rows=new ArrayList<>();
        for(Spell spell:Spell.values())for(Phase phase:Phase.values()){
            Map<String,Object> row=new LinkedHashMap<>();row.put("id",spell.id);row.put("ordinal",spell.ordinal());row.put("form",spell.form.name());row.put("color",spell.color);row.put("radius",spell.radius);row.put("duration",spell.duration);row.put("attached",spell.attached);row.put("phase",phase.name());row.put("phaseOrdinal",phase.ordinal());row.put("lifetime",spell.lifetime(phase));row.put("followsCaster",spell.followsCaster(phase));row.put("findIdentity",SpellVisualRules.find(spell.id)==spell);rows.add(row);
        }
        Files.writeString(Path.of(arguments[0]),new GsonBuilder().setPrettyPrinting().create().toJson(rows));
        System.out.println("rows="+rows.size());
    }
}
