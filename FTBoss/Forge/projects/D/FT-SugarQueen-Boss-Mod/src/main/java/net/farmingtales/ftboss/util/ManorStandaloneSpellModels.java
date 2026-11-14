package net.farmingtales.ftboss.util;

import java.util.EnumSet;
import java.util.Set;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

/** Solid, depth-writing meshes for Manor spells with no Iron-native model or particle presentation. */
public final class ManorStandaloneSpellModels {
    public static final int MAX_MODELS_PER_FRAME = 12;
    public static final int MAX_QUADS = 2600;
    public static final int CHARGED_ORB_REFERENCE_QUADS = RemainingSpellModels.CHARGED_ORB_REFERENCE_QUADS;
    private static final Set<Spell> MODELED = EnumSet.of(
            Spell.CANDLE_PRISON, Spell.RENDING_CLEAVE, Spell.CRIMSON_OATH, Spell.SEARING_PAN,
            Spell.FOUR_TIDES, Spell.ORBITAL_GEARS, Spell.SHADOW_BRAND, Spell.VOID_ECHO,
            Spell.HALO_SANCTUARY, Spell.BLOOD_VERDICT, Spell.CITRUS_NOVA,
            Spell.WINTER_CLOCK, Spell.FEATHER_REFUGE);

    public record Point(double x, double y, double z) {
        Point add(Point p) { return new Point(x + p.x, y + p.y, z + p.z); }
        Point sub(Point p) { return new Point(x - p.x, y - p.y, z - p.z); }
        Point scale(double n) { return new Point(x * n, y * n, z * n); }
        double length() { return Math.sqrt(x * x + y * y + z * z); }
        Point normal() { double n = length(); return n < 1E-9 ? new Point(0, 1, 0) : scale(1 / n); }
        Point cross(Point p) { return new Point(y * p.z - z * p.y, z * p.x - x * p.z, x * p.y - y * p.x); }
    }
    @FunctionalInterface public interface Sink { void quad(Point a, Point b, Point c, Point d, int color); }

    private ManorStandaloneSpellModels() {}
    public static Set<Spell> modeled() { return Set.copyOf(MODELED); }
    public static boolean supports(Spell spell) { return MODELED.contains(spell); }
    public static boolean renders(Spell spell, Phase phase) { return supports(spell) && phase != Phase.BEAM; }

    public static int draw(Spell spell, Phase phase, double age, Sink output) {
        if (!renders(spell, phase) || !Double.isFinite(age) || age < 0 || output == null) return 0;
        Mesh mesh = new Mesh(output);
        double enter = phase == Phase.CHARGE ? 0.18 + smooth((age + 1) / 9) * 0.72 : 1;
        double pulse = phase == Phase.PULSE ? 1 + Math.sin(age * 0.28) * 0.055 : 1;
        Sink sink = transform(0, 0, 0, age * 0.006, 0, 0, enter * pulse, mesh::face);
        switch (spell) {
            case CANDLE_PRISON -> candlePrison(age, sink);
            case RENDING_CLEAVE -> rendingCleave(age, sink);
            case CRIMSON_OATH -> crimsonOath(age, sink);
            case SEARING_PAN -> searingPan(age, sink);
            case FOUR_TIDES -> fourTides(age, sink);
            case ORBITAL_GEARS -> orbitalGears(age, sink);
            case SHADOW_BRAND -> shadowBrand(age, sink);
            case VOID_ECHO -> voidEcho(age, sink);
            case HALO_SANCTUARY -> haloSanctuary(age, sink);
            case BLOOD_VERDICT -> bloodVerdict(age, sink);
            case CITRUS_NOVA -> citrusNova(age, sink);
            case WINTER_CLOCK -> winterClock(age, sink);
            case FEATHER_REFUGE -> featherRefuge(age, sink);
            default -> { }
        }
        return mesh.count;
    }

    private static void candlePrison(double age, Sink s) {
        int wax=0xE3D8D5, metal=0x342D45, violet=0x9E72C4, flame=0xF7D58B;
        for (int i=0;i<8;i++) {
            double a=i*Math.PI/4+age*0.009; Point foot=polar(2.55,a,0.12);
            candle(foot,1.25+(i&1)*0.35,wax,metal,flame,s);
            strut(foot.add(new Point(0,0.65,0)),polar(2.55,a+Math.PI/4,0.72+(i&1)*0.16),0.045,metal,s);
            strut(foot.add(new Point(0,1.02,0)),new Point(0,1.85,0),0.032,violet,s);
        }
        taper(new Point(0,0.25,0),0.58,0.42,0.76,8,metal,s);
        box(new Point(0,1.12,0),0.42,0.48,0.22,age*0.018,0,0,violet,s);
        arcTorus(new Point(0,1.58,0),0.54,0.08,14,5,-Math.PI*.92,Math.PI*1.84,Math.PI/2,0,0,metal,s);
        crystal(new Point(0,1.08,-0.25),0.12,0.34,6,flame,s);
    }

    private static void rendingCleave(double age, Sink s) {
        for (int i=0;i<3;i++) {
            double side=(i-1)*1.25, roll=(i-1)*-.16+Math.sin(age*.08+i)*.025;
            cleaver(0x25232E,0x776A79,0xC65570,0xF19AAA,transform(side,.1+i*.24,1.8+i*.8,0,0,roll,1,s));
        }
        for (int i=-2;i<=2;i++) strut(new Point(i*.8,.08,.7),new Point(i*1.2,.12,4.8),.035,0xC65570,s);
    }

    private static void crimsonOath(double age, Sink s) {
        crystal(new Point(0,1.55,0),.42,1.15,8,0xF16D79,s);
        torus(new Point(0,1.55,0),1,.075,18,5,age*.035,Math.PI/2,0,0xC89A62,s);
        torus(new Point(0,1.55,0),1.32,.045,20,4,-age*.025,-.65,.25,0xA83F55,s);
        for(int i=0;i<3;i++) { double a=age*.022+i*Math.PI*2/3; Point p=polar(1.8,a,1.15+Math.sin(age*.12+i)*.14);
            shieldTablet(0x3A2430,0xA83F55,0xC89A62,transform(p.x,p.y,p.z,-a,0,0,.8,s)); }
        taper(new Point(0,.08,0),1.55,1.25,.18,12,0x3A2430,s);
    }

    private static void searingPan(double age, Sink s) {
        Sink pan=transform(0,.82,2.15,.15,-.18,Math.sin(age*.08)*.06,1,s);
        taper(new Point(0,0,0),1.08,.82,.28,16,0x2E3036,pan);
        torus(new Point(0,.25,0),1.05,.09,20,5,0,0,0,0x76777A,pan);
        strut(new Point(0,.14,-.8),new Point(0,.23,-2.75),.16,0x2E3036,pan);
        box(new Point(0,.23,-2.7),.24,.18,.38,0,0,0,0xC06B3E,pan);
        for(int i=0;i<7;i++){double a=i*Math.PI*2/7+age*.03; crystal(polar(.55,a,.28),.16,.7+(i%3)*.18,5,i%2==0?0xFFC56D:0xF27843,pan);}
        arcTorus(new Point(0,.7,1.8),2.4,.13,18,5,Math.PI*.12,Math.PI*.76,Math.PI/2,0,0,0xFFC56D,s);
    }

    private static void fourTides(double age, Sink s) {
        for(int i=0;i<4;i++) { double x=(i-1.5)*1.15,z=1.35+i*.72+Math.min(1,age/10)*1.2;
            Sink t=transform(x,.25+Math.sin(age*.09+i)*.14,z,0,-.08,0,.82,s);
            strut(new Point(0,.65,-1.1),new Point(0,.65,1.3),.07,0x245C73,t);
            for(int f=-1;f<=1;f++){Point root=new Point(f*.34,.65,.7),tip=new Point(f*.48,.65+(f==0?.16:0),1.65); strut(root,tip,.075,f==0?0xD6FFFF:0x64D9DA,t); crystal(tip,.1,.4,5,0xD6FFFF,t);}
            strut(new Point(-.48,.65,.75),new Point(.48,.65,.75),.065,0x789EAC,t);
            for(int f:new int[]{-1,1}) extrude(new Point[]{new Point(0,0,0),new Point(f*.75,.1,-.55),new Point(f*.42,.12,.45),new Point(0,0,.8)},.035,0x64D9DA,0x245C73,t);
        }
    }

    private static void orbitalGears(double age, Sink s) {
        for(int i=0;i<3;i++){double a=age*.045+i*Math.PI*2/3;Point p=polar(2.15,a,1.05+i*.28);
            Sink w=transform(p.x,p.y,p.z,-a,Math.PI/2,age*(i%2==0?.08:-.07),.9,s);
            gear(new Point(0,0,0),.84,.58,.15,10,0x526D78,0xA0DCE7,w); torus(new Point(0,0,0),.32,.075,14,5,0,0,0,0xC6A56B,w); crystal(new Point(0,0,0),.18,.55,6,0xA0DCE7,w); strut(p,new Point(0,1.55,0),.025,0x273842,s);}
        gear(new Point(0,.16,0),1.15,.92,.09,14,0x273842,0x526D78,transform(0,0,0,-age*.015,0,0,1,s));
    }

    private static void shadowBrand(double age, Sink s) {
        taper(new Point(0,.15,0),.68,.38,2.1,6,0x1A1526,s); crystal(new Point(0,2,0),.38,1,6,0x9C79CE,s);
        torus(new Point(0,1.45,0),1.1,.07,18,4,age*.03,Math.PI/2,0,0xD7B7F2,s);
        for(int i=0;i<6;i++){double a=i*Math.PI/3-age*.012;Point foot=polar(2.7,a,.08),head=polar(2.2,a,1.75);strut(foot,head,.12,0x1A1526,s);crystal(head,.22,.72,5,0x4B3569,s);strut(head,new Point(0,1.45,0),.045,0x9C79CE,s);}
        gear(new Point(0,.1,0),2.05,1.72,.055,12,0x4B3569,0x1A1526,transform(0,0,0,age*.01,0,0,1,s));
    }
    private static void voidEcho(double age, Sink s) {
        crystal(new Point(0,1.35,0),.42,1.2,8,0x171321,s);
        for(int r=0;r<3;r++) torus(new Point(0,1.35,0),.8+r*.48,.07-r*.01,20,5,age*(r%2==0?.035:-.028),Math.PI/2+r*.48,r*.31,r==2?0xCB94E4:0x694A8B,s);
        for(int side:new int[]{-1,1}) { Sink bell=transform(side*1.65,1.4,0,side*-Math.PI/2,0,0,.9,s);
            taper(new Point(0,-.5,0),.55,.23,.85,10,0x694A8B,bell); torus(new Point(0,.32,0),.55,.07,16,4,0,0,0,0xE8D4F4,bell); crystal(new Point(0,-.68,0),.12,.34,5,0xCB94E4,bell);
            for(int w=0;w<3;w++) arcTorus(new Point(0,.15,side*.25),.75+w*.32,.035,12,4,-Math.PI*.36,Math.PI*.72,Math.PI/2,0,0,0xCB94E4,bell); }
    }

    private static void haloSanctuary(double age, Sink s) {
        taper(new Point(0,.1,0),.95,.72,.4,12,0x665E66,s); taper(new Point(0,.48,0),.68,.42,1.35,8,0xE8DFC8,s); crystal(new Point(0,1.82,0),.3,.9,8,0xF1DFA0,s);
        torus(new Point(0,2.25,0),.82,.08,18,5,age*.018,Math.PI/2,0,0xC8A85C,s);
        for(int i=0;i<8;i++){double a=i*Math.PI/4+age*.005;Point foot=polar(3,a,.12),shoulder=polar(2.45,a,2.15),crown=polar(1.28,a,3.15);strut(foot,shoulder,.09,0x665E66,s);strut(shoulder,crown,.075,0xC8A85C,s);crystal(shoulder,.15,.5,6,0xF1DFA0,s);}
        torus(new Point(0,3.18,0),1.28,.07,20,5,0,0,0,0xE8DFC8,s);
    }

    private static void bloodVerdict(double age, Sink s) {
        Sink sword=transform(0,.18+Math.max(0,1-Math.min(1,age/10))*2.2,0,0,0,0,1.1,s);
        Point[] blade={new Point(-.34,0,0),new Point(-.48,2.55,0),new Point(0,3.45,0),new Point(.48,2.55,0),new Point(.34,0,0),new Point(0,-.72,0)};
        extrude(blade,.12,0x8A727A,0x27212A,sword); strut(new Point(-1,2.72,0),new Point(1,2.72,0),.12,0xC9965C,sword); strut(new Point(0,2.7,0),new Point(0,4.05,0),.14,0x27212A,sword); crystal(new Point(0,4.18,0),.24,.58,6,0xF06483,sword);
        for(int side:new int[]{-1,1}){Point chain=new Point(side*1.65,2.6,0);strut(new Point(side*.75,2.72,0),chain,.035,0xC9965C,sword);taper(chain.add(new Point(0,-.5,0)),.58,.42,.16,8,0xF06483,sword);box(chain.add(new Point(0,-.65,0)),.52,.07,.38,0,0,0,0x27212A,sword);}
        for(int inlay=0;inlay<7;inlay++){double y=.32+inlay*.39;double width=.26*(1-y/4.2);crystal(new Point(-width,y,-.16),.045,.14,8,inlay%2==0?0xF6B0B9:0xC9965C,sword);crystal(new Point(width,y,-.16),.045,.14,8,inlay%2==0?0xF6B0B9:0xC9965C,sword);}
        torus(new Point(0,2.72,0),.42,.045,20,6,0,Math.PI/2,0,0xC9965C,sword);
        for(int r=0;r<8;r++)strut(new Point(0,.08,0),polar(2.7,r*Math.PI/4,.08),.045,0xF06483,s);
    }

    private static void citrusNova(double age, Sink s) {
        Sink fruit=transform(0,1.55,0,age*.026,0,0,1,s); orb(new Point(0,0,0),1.08,8,12,0xE59D37,fruit);
        for(int i=0;i<8;i++)torus(new Point(0,0,0),1.09,.035,18,4,i*Math.PI/4,Math.PI/2,0,i%2==0?0xFFE5A0:0xFFD15B,fruit);
        torus(new Point(0,0,0),1.1,.055,20,4,0,0,0,0x7B512A,fruit);
        for(int i=0;i<2;i++){Sink leaf=transform(0,1,0,i==0?-.55:.7,-.5,0,1,fruit);extrude(new Point[]{new Point(0,0,0),new Point(-.5,.42,0),new Point(0,1.12,0),new Point(.5,.42,0)},.055,0x6DA85A,0x3E713D,leaf);}
        for(int i=0;i<10;i++){double a=i*Math.PI/5+age*.012;Point base=polar(1.5,a,1.55),tip=polar(2.7+Math.sin(age*.12+i)*.18,a,1.55);strut(base,tip,.07,i%2==0?0xFFD15B:0xFFE5A0,s);crystal(tip,.12,.42,5,0xFFD15B,s);}
    }

    private static void winterClock(double age, Sink s) {
        Sink face=transform(0,2.35,0,0,Math.PI/2,0,1,s);torus(new Point(0,0,0),1.75,.13,24,6,0,0,0,0x527D9A,face);torus(new Point(0,0,0),1.43,.045,20,4,0,0,0,0xE8F8FF,face);
        gear(new Point(0,0,.12),1.25,.98,.07,12,0x273C55,0x9EDEFF,transform(0,0,0,-age*.018,0,0,1,face));
        for(int i=0;i<12;i++){double a=i*Math.PI/6;crystal(new Point(Math.cos(a)*1.5,Math.sin(a)*1.5,-.1),.09,i%3==0?.48:.3,5,i%3==0?0xE8F8FF:0x9EDEFF,face);}
        double minute=-age*.055,hour=-age*.009;strut(new Point(0,0,-.18),new Point(Math.cos(minute)*1.2,Math.sin(minute)*1.2,-.18),.055,0xE8F8FF,face);strut(new Point(0,0,-.22),new Point(Math.cos(hour)*.82,Math.sin(hour)*.82,-.22),.085,0x527D9A,face);crystal(new Point(0,0,-.25),.16,.4,6,0x9EDEFF,face);
        for(int i=0;i<6;i++)crystal(polar(2.55,i*Math.PI/3+age*.01,.12),.2,1.1+(i%2)*.45,6,0x9EDEFF,s);
    }

    private static void featherRefuge(double age, Sink s) {
        for(int i=0;i<10;i++){double a=i*Math.PI/5+age*.004;Point foot=polar(3.1,a,.15);feather(0xD9CEB9,0xFFF3D6,0x7F715F,transform(foot.x,foot.y,foot.z,-a,-.48,0,1.15,s));strut(foot.add(new Point(0,.15,0)),polar(.55,a,3.45),.035,0xC8A369,s);}
        torus(new Point(0,3.55,0),.72,.09,20,5,age*.02,0,0,0xC8A369,s);crystal(new Point(0,3.55,0),.25,.85,7,0xFFF3D6,s);
        gear(new Point(0,.08,0),2.55,2.28,.045,20,0x7F715F,0xD9CEB9,transform(0,0,0,-age*.006,0,0,1,s));
    }

    private static void candle(Point foot,double h,int wax,int metal,int flame,Sink s){taper(foot,.22,.22,h,8,wax,s);taper(foot.add(new Point(0,-.05,0)),.32,.27,.16,8,metal,s);torus(foot.add(new Point(0,h*.92,0)),.22,.035,12,4,0,0,0,metal,s);crystal(foot.add(new Point(0,h+.2,0)),.12,.48,5,flame,s);}
    private static void cleaver(int iron,int steel,int blood,int edge,Sink s){
        Point[] blade={new Point(-1.7,.05,0),new Point(-1.48,1,0),new Point(-.85,1.5,0),new Point(1.25,1.38,0),new Point(1.72,.75,0),new Point(1.42,.18,0)};
        extrude(blade,.13,steel,iron,s);
        extrude(new Point[]{new Point(-1.55,.08,-.14),new Point(1.38,.2,-.14),new Point(1.62,.62,-.14),new Point(-1.25,.45,-.14)},.025,edge,blood,s);
        strut(new Point(-1.48,.18,-.18),new Point(1.42,.29,-.18),.026,edge,s);
        strut(new Point(-1.12,1.18,-.18),new Point(1.22,1.08,-.18),.021,blood,s);
        for(int rivet=0;rivet<5;rivet++) crystal(new Point(-1.15+rivet*.52,.72+(rivet&1)*.12,-.2),.055,.15,8,edge,s);
        strut(new Point(-.95,1.25,0),new Point(-1.38,2.25,0),.14,iron,s);
        torus(new Point(-1.25,1.75,0),.23,.035,18,6,0,Math.PI/2,0,edge,s);
        box(new Point(-1.43,2.38,0),.22,.38,.2,0,0,-.38,blood,s);
    }
    private static void shieldTablet(int dark,int color,int trim,Sink s){Point[] body={new Point(-.52,-.72,0),new Point(-.58,.48,0),new Point(0,.86,0),new Point(.58,.48,0),new Point(.52,-.72,0),new Point(0,-1.05,0)};extrude(body,.12,color,dark,s);strut(new Point(-.37,.35,-.14),new Point(.37,.35,-.14),.035,trim,s);strut(new Point(0,-.55,-.14),new Point(0,.62,-.14),.04,trim,s);crystal(new Point(0,.12,-.2),.12,.38,6,trim,s);}
    private static void feather(int body,int edge,int quill,Sink s){Point[] p={new Point(0,0,0),new Point(-.38,.55,0),new Point(-.52,1.5,0),new Point(-.22,2.55,0),new Point(0,3.05,0),new Point(.22,2.55,0),new Point(.52,1.5,0),new Point(.38,.55,0)};extrude(p,.055,body,quill,s);strut(new Point(0,-.25,-.08),new Point(0,2.75,-.08),.045,quill,s);for(int i=1;i<=5;i++){double y=.35+i*.4,w=.5-i*.055;strut(new Point(0,y,-.09),new Point(-w,y+.2,-.09),.024,edge,s);strut(new Point(0,y,-.09),new Point(w,y+.2,-.09),.024,edge,s);}}
    private static void box(Point c,double hx,double hy,double hz,double yaw,double pitch,double roll,int color,Sink s){
        Point[] p=new Point[8];int n=0;for(int y:new int[]{-1,1})for(int z:new int[]{-1,1})for(int x:new int[]{-1,1})p[n++]=pose(new Point(x*hx,y*hy,z*hz),c.x,c.y,c.z,yaw,pitch,roll,1);
        int[][] f={{0,1,3,2},{4,6,7,5},{0,4,5,1},{2,3,7,6},{0,2,6,4},{1,5,7,3}};for(int[] q:f)shade(p[q[0]],p[q[1]],p[q[2]],p[q[3]],color,s);
    }
    private static void taper(Point bottom,double br,double tr,double h,int sides,int color,Sink s){
        sides=Math.max(12,sides);Point top=bottom.add(new Point(0,h,0));for(int i=0;i<sides;i++){double a=i*Math.PI*2/sides,b=(i+1)*Math.PI*2/sides;Point ba=bottom.add(new Point(Math.cos(a)*br,0,Math.sin(a)*br)),bb=bottom.add(new Point(Math.cos(b)*br,0,Math.sin(b)*br)),ta=top.add(new Point(Math.cos(a)*tr,0,Math.sin(a)*tr)),tb=top.add(new Point(Math.cos(b)*tr,0,Math.sin(b)*tr));shade(ba,bb,tb,ta,color,s);shade(bottom,bb,ba,ba,mix(color,0x171521,.28),s);shade(top,ta,tb,tb,mix(color,0xFFFFFF,.12),s);}}
    private static void crystal(Point c,double r,double h,int sides,int color,Sink s){sides=Math.max(10,sides);Point bottom=c.add(new Point(0,-h*.52,0)),top=c.add(new Point(0,h*.52,0));for(int i=0;i<sides;i++){double a=i*Math.PI*2/sides,b=(i+1)*Math.PI*2/sides;Point l=c.add(new Point(Math.cos(a)*r,0,Math.sin(a)*r)),q=c.add(new Point(Math.cos(b)*r,0,Math.sin(b)*r));shade(bottom,q,l,l,mix(color,0x171521,.2),s);shade(l,q,top,top,color,s);}}
    private static void orb(Point c,double r,int bands,int sides,int color,Sink s){bands=Math.max(12,bands);sides=Math.max(18,sides);for(int y=0;y<bands;y++){double low=-Math.PI/2+y*Math.PI/bands,high=-Math.PI/2+(y+1)*Math.PI/bands;for(int i=0;i<sides;i++){double a=i*Math.PI*2/sides,b=(i+1)*Math.PI*2/sides;shade(c.add(spherical(r,low,a)),c.add(spherical(r,low,b)),c.add(spherical(r,high,b)),c.add(spherical(r,high,a)),mix(color,i%3==0?0xFFFFFF:0x251C29,i%3==0?.16:.08),s);}}}
    private static void torus(Point c,double r,double tube,int seg,int sides,double yaw,double pitch,double roll,int color,Sink s){arcTorus(c,r,tube,seg,sides,0,Math.PI*2,yaw,pitch,roll,color,s);}
    private static void arcTorus(Point c,double r,double tube,int seg,int sides,double start,double arc,double yaw,double pitch,double roll,int color,Sink s){seg=Math.max(24,seg);sides=Math.max(6,sides);Sink t=transform(c.x,c.y,c.z,yaw,pitch,roll,1,s);for(int i=0;i<seg;i++){double a=start+arc*i/seg,b=start+arc*(i+1)/seg;for(int j=0;j<sides;j++){double u=j*Math.PI*2/sides,v=(j+1)*Math.PI*2/sides;shade(torusPoint(r,tube,a,u),torusPoint(r,tube,b,u),torusPoint(r,tube,b,v),torusPoint(r,tube,a,v),color,t);}}}
    private static void gear(Point c,double outer,double inner,double hh,int teeth,int body,int edge,Sink s){int count=teeth*4;Point top=c.add(new Point(0,hh,0)),bottom=c.add(new Point(0,-hh,0));for(int i=0;i<count;i++){double a=i*Math.PI*2/count,b=(i+1)*Math.PI*2/count,ra=i%4<2?outer:inner,rb=(i+1)%4<2?outer:inner;Point ta=c.add(new Point(Math.cos(a)*ra,hh,Math.sin(a)*ra)),tb=c.add(new Point(Math.cos(b)*rb,hh,Math.sin(b)*rb)),ba=ta.add(new Point(0,-hh*2,0)),bb=tb.add(new Point(0,-hh*2,0));shade(top,tb,ta,ta,edge,s);shade(bottom,ba,bb,bb,mix(body,0x101018,.3),s);shade(ba,ta,tb,bb,body,s);}}
    private static void strut(Point a,Point b,double w,int color,Sink s){Point d=b.sub(a).normal(),ref=Math.abs(d.y)<.88?new Point(0,1,0):new Point(1,0,0),u=d.cross(ref).normal(),v=d.cross(u).normal();int facets=8;Point[] low=new Point[facets],high=new Point[facets];for(int i=0;i<facets;i++){double angle=i*Math.PI*2/facets;Point offset=u.scale(Math.cos(angle)*w*1.32).add(v.scale(Math.sin(angle)*w*1.32));low[i]=a.add(offset);high[i]=b.add(offset);}for(int i=0;i<facets;i++){int q=(i+1)%facets;shade(low[i],low[q],high[q],high[i],color,s);}}
    private static void extrude(Point[] p,double depth,int frontColor,int sideColor,Sink s){Point front=average(p).add(new Point(0,0,-depth)),back=average(p).add(new Point(0,0,depth));for(int i=0;i<p.length;i++){int q=(i+1)%p.length;Point a=p[i].add(new Point(0,0,-depth)),b=p[q].add(new Point(0,0,-depth)),c=p[i].add(new Point(0,0,depth)),d=p[q].add(new Point(0,0,depth));shade(front,a,b,b,frontColor,s);shade(back,d,c,c,mix(frontColor,0x171521,.25),s);shade(a,c,d,b,sideColor,s);}}
    private static Point average(Point[] points){Point sum=new Point(0,0,0);for(Point p:points)sum=sum.add(p);return sum.scale(1d/points.length);}

    private static Sink transform(double x,double y,double z,double yaw,double pitch,double roll,double scale,Sink sink){return(a,b,c,d,color)->sink.quad(pose(a,x,y,z,yaw,pitch,roll,scale),pose(b,x,y,z,yaw,pitch,roll,scale),pose(c,x,y,z,yaw,pitch,roll,scale),pose(d,x,y,z,yaw,pitch,roll,scale),color);}
    private static Point pose(Point p,double ox,double oy,double oz,double yaw,double pitch,double roll,double scale){double x=p.x*scale,y=p.y*scale,z=p.z*scale,rx=x*Math.cos(roll)-y*Math.sin(roll),ry=x*Math.sin(roll)+y*Math.cos(roll),py=ry*Math.cos(pitch)-z*Math.sin(pitch),pz=ry*Math.sin(pitch)+z*Math.cos(pitch);return new Point(ox+rx*Math.cos(yaw)+pz*Math.sin(yaw),oy+py,oz-rx*Math.sin(yaw)+pz*Math.cos(yaw));}
    private static Point polar(double r,double a,double y){return new Point(Math.cos(a)*r,y,Math.sin(a)*r);}
    private static Point spherical(double r,double e,double a){double h=Math.cos(e)*r;return new Point(Math.cos(a)*h,Math.sin(e)*r,Math.sin(a)*h);}
    private static Point torusPoint(double r,double t,double a,double side){double d=r+Math.cos(side)*t;return new Point(Math.cos(a)*d,Math.sin(side)*t,Math.sin(a)*d);}
    private static void shade(Point a,Point b,Point c,Point d,int color,Sink s){Point n=b.sub(a).cross(c.sub(a));double len=n.length(),light=len<1E-9?.82:.62+.38*Math.abs((n.x*.38+n.y*.82+n.z*.43)/len);s.quad(a,b,c,d,multiply(color,light));}
    private static int multiply(int c,double n){int r=Math.min(255,(int)Math.round((c>>16&255)*n)),g=Math.min(255,(int)Math.round((c>>8&255)*n)),b=Math.min(255,(int)Math.round((c&255)*n));return r<<16|g<<8|b;}
    private static int mix(int a,int b,double n){int c=0;for(int shift=0;shift<=16;shift+=8)c|=(int)Math.round((a>>shift&255)*(1-n)+(b>>shift&255)*n)<<shift;return c;}
    private static double smooth(double n){double v=Math.max(0,Math.min(1,n));return v*v*(3-2*v);}
    private static final class Mesh {private final Sink output;private int count;private Mesh(Sink output){this.output=output;}private void face(Point a,Point b,Point c,Point d,int color){if(count>=MAX_QUADS||b.sub(a).cross(c.sub(a)).length()+c.sub(a).cross(d.sub(a)).length()<1E-9)return;output.quad(a,b,c,d,color);count++;}}
}