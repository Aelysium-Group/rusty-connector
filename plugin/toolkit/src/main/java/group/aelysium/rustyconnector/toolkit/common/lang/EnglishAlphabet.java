package group.aelysium.rustyconnector.toolkit.common.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.newlines;

public class EnglishAlphabet implements ASCIIAlphabet {
    protected int font_size = 6;

    protected Component WHITESPACE() {
        return join(
                JoinConfiguration.noSeparators(),
                text("     "),
                text("     "),
                text("     "),
                text("     "),
                text("     "),
                text("     ")
        );
    }

    protected Component A() {
        return join(
                JoinConfiguration.noSeparators(),
                text(" █████╗ "),
                text("██╔══██╗"),
                text("███████║"),
                text("██╔══██║"),
                text("██║  ██║"),
                text("╚═╝  ╚═╝")
        );
    }

    protected Component B() {
        return join(
            JoinConfiguration.noSeparators(),
            text("██████╗ "),
            text("██╔══██╗"),
            text("██████╔╝"),
            text("██╔══██╗"),
            text("██████╔╝"),
            text("╚═════╝ ")
        );
    }

    protected Component C() {
        return join(
                JoinConfiguration.noSeparators(),
                text(" ██████╗"),
                text("██╔════╝"),
                text("██║     "),
                text("██║     "),
                text("╚██████╗"),
                text(" ╚═════╝")
        );
    }
    protected Component D() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██████╗ "),
                text("██╔══██╗"),
                text("██║  ██║"),
                text("██║  ██║"),
                text("██████╔╝"),
                text("╚═════╝ ")
        );
    }
    protected Component E() {
        return join(
                JoinConfiguration.noSeparators(),
                text("███████╗"),
                text("██╔════╝"),
                text("█████╗  "),
                text("██╔══╝  "),
                text("███████╗"),
                text("╚══════╝")
        );
    }
    protected Component F() {
        return join(
                JoinConfiguration.noSeparators(),
                text("███████╗"),
                text("██╔════╝"),
                text("█████╗  "),
                text("██╔══╝  "),
                text("██║     "),
                text("╚═╝     ")
        );
    }
    protected Component G() {
        return join(
                JoinConfiguration.noSeparators(),
                text(" ██████╗ "),
                text("██╔════╝ "),
                text("██║  ███╗"),
                text("██║   ██║"),
                text("╚██████╔╝"),
                text(" ╚═════╝ ")
        );
    }
    protected Component H() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗  ██╗"),
                text("██║  ██║"),
                text("███████║"),
                text("██╔══██║"),
                text("██║  ██║"),
                text("╚═╝  ╚═╝")
        );
    }
    protected Component I() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗"),
                text("██║"),
                text("██║"),
                text("██║"),
                text("██║"),
                text("╚═╝")
        );
    }
    protected Component J() {
        return join(
                JoinConfiguration.noSeparators(),
                text("     ██╗"),
                text("     ██║"),
                text("     ██║"),
                text("██   ██║"),
                text("╚█████╔╝"),
                text(" ╚════╝ ")
        );
    }
    protected Component K() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗  ██╗"),
                text("██║ ██╔╝"),
                text("█████╔╝ "),
                text("██╔═██╗ "),
                text("██║  ██╗"),
                text("╚═╝  ╚═╝")
        );
    }
    protected Component L() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗     "),
                text("██║     "),
                text("██║     "),
                text("██║     "),
                text("███████╗"),
                text("╚══════╝")
        );
    }
    protected Component M() {
        return join(
                JoinConfiguration.noSeparators(),
                text("███╗   ███╗"),
                text("████╗ ████║"),
                text("██╔████╔██║"),
                text("██║╚██╔╝██║"),
                text("██║ ╚═╝ ██║"),
                text("╚═╝     ╚═╝")
        );
    }
    protected Component N() {
        return join(
                JoinConfiguration.noSeparators(),
                text("███╗   ██╗"),
                text("████╗  ██║"),
                text("██╔██╗ ██║"),
                text("██║╚██╗██║"),
                text("██║ ╚████║"),
                text("╚═╝  ╚═══╝")
        );
    }
    protected Component O() {
        return join(
                JoinConfiguration.noSeparators(),
                text(" ██████╗ "),
                text("██╔═══██╗"),
                text("██║   ██║"),
                text("██║   ██║"),
                text("╚██████╔╝"),
                text(" ╚═════╝ ")
        );
    }
    protected Component P() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██████╗ "),
                text("██╔══██╗"),
                text("██████╔╝"),
                text("██╔═══╝ "),
                text("██║     "),
                text("╚═╝     ")
        );
    }
    protected Component Q() {
        return join(
                JoinConfiguration.noSeparators(),
                text(" ██████╗ "),
                text("██╔═══██╗"),
                text("██║   ██║"),
                text("██║▄▄ ██║"),
                text("╚██████╔╝"),
                text(" ╚══▀▀═╝ ")
        );
    }
    protected Component R() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██████╗ "),
                text("██╔══██╗"),
                text("██████╔╝"),
                text("██╔══██╗"),
                text("██║  ██║"),
                text("╚═╝  ╚═╝")
        );
    }
    protected Component S() {
        return join(
                JoinConfiguration.noSeparators(),
                text("███████╗"),
                text("██╔════╝"),
                text("███████╗"),
                text("╚════██║"),
                text("███████║"),
                text("╚══════╝")
        );
    }
    protected Component T() {
        return join(
                JoinConfiguration.noSeparators(),
                text("████████╗"),
                text("╚══██╔══╝"),
                text("   ██║   "),
                text("   ██║   "),
                text("   ██║   "),
                text("   ╚═╝   ")
        );
    }
    protected Component U() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗   ██╗"),
                text("██║   ██║"),
                text("██║   ██║"),
                text("██║   ██║"),
                text("╚██████╔╝"),
                text(" ╚═════╝ ")
        );
    }
    protected Component V() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗   ██╗"),
                text("██║   ██║"),
                text("██║   ██║"),
                text("╚██╗ ██╔╝"),
                text(" ╚████╔╝ "),
                text("  ╚═══╝  ")
        );
    }
    protected Component W() {
        return join(
                JoinConfiguration.noSeparators(),
                text(" ██╗    ██╗"),
                text(" ██║    ██║"),
                text(" ██║ █╗ ██║"),
                text(" ██║███╗██║"),
                text(" ╚███╔███╔╝"),
                text("  ╚══╝╚══╝ ")
        );
    }
    protected Component X() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗  ██╗"),
                text("╚██╗██╔╝"),
                text(" ╚███╔╝ "),
                text(" ██╔██╗ "),
                text("██╔╝ ██╗"),
                text("╚═╝  ╚═╝")
        );
    }
    protected Component Y() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗   ██╗"),
                text("╚██╗ ██╔╝"),
                text(" ╚████╔╝ "),
                text("  ╚██╔╝  "),
                text("   ██║   "),
                text("   ╚═╝   ")
        );
    }
    protected Component Z() {
        return join(
                JoinConfiguration.noSeparators(),
                text("███████╗"),
                text("╚══███╔╝"),
                text("  ███╔╝ "),
                text(" ███╔╝  "),
                text("███████╗"),
                text("╚══════╝")
        );
    }
    protected Component PERIOD() {
        return join(
                JoinConfiguration.noSeparators(),
                text("     "),
                text("     "),
                text("     "),
                text("████╗"),
                text("████║"),
                text("╚═══╝")
        );
    }
    protected Component EXCLAMATION() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██╗"),
                text("██║"),
                text("██║"),
                text("╚═╝"),
                text("██╗"),
                text("╚═╝")
        );
    }
    protected Component QUESTION() {
        return join(
                JoinConfiguration.noSeparators(),
                text("██████╗ "),
                text("╚════██╗"),
                text("  ▄███╔╝"),
                text("  ▀▀══╝ "),
                text("  ██╗   "),
                text("  ╚═╝   ")
        );
    }
    protected Component DASH() {
        return join(
                JoinConfiguration.noSeparators(),
                text("      "),
                text("      "),
                text("█████╗"),
                text("╚════╝"),
                text("      "),
                text("      ")
        );
    }
    protected Component UNDERSCORE() {
        return join(
                JoinConfiguration.noSeparators(),
                text("        "),
                text("        "),
                text("        "),
                text("        "),
                text("███████╗"),
                text("╚══════╝")
        );
    }

    /**
     * Converts a string to an ASCIIAlphabet equivalent.
     * This method does not recognize, numbers, punctuation, nor newlines.
     */
    @Override
    public Component generate(String string) {
        Map<Character, Component> map = map();
        List<Component> generatedString = new ArrayList<>();

        for (int i = 0; i < this.font_size; i++) {
            generatedString.add(text(""));
        }

        for(char character : string.toUpperCase().toCharArray()) {
            try {
                Component asciiCharacter = map.get(character);
                for (int i = 0; i < this.font_size; i++) {
                    Component receivingRow = generatedString.get(i);
                    Component rowToExtract = asciiCharacter.children().get(i);

                    generatedString.set(i,receivingRow.append(rowToExtract));
                }
            } catch (Exception ignored) {}
        }

        return join(
                newlines(),
                generatedString.get(0),
                generatedString.get(1),
                generatedString.get(2),
                generatedString.get(3),
                generatedString.get(4),
                generatedString.get(5)
        );
    }

    /**
     * Converts a string to an ASCIIAlphabet equivalent.
     * This method does not recognize, numbers, punctuation, nor newlines.
     */
    @Override
    public Component generate(String string, NamedTextColor color) {
        Map<Character, Component> map = map();
        List<Component> generatedString = new ArrayList<>();

        for (int i = 0; i < this.font_size; i++) {
            generatedString.add(text(""));
        }

        for(char character : string.toUpperCase().toCharArray()) {
            try {
                Component asciiCharacter = map.get(character);
                for (int i = 0; i < this.font_size; i++) {
                    Component receivingRow = generatedString.get(i);
                    Component rowToExtract = asciiCharacter.children().get(i);

                    generatedString.set(i,receivingRow.append(rowToExtract));
                }
            } catch (Exception ignored) {}
        }

        return join(
                newlines(),
                generatedString.get(0).color(color),
                generatedString.get(1).color(color),
                generatedString.get(2).color(color),
                generatedString.get(3).color(color),
                generatedString.get(4).color(color),
                generatedString.get(5).color(color)
        );
    }

    protected Map<Character, Component> map() {
        Map<Character, Component> output = new HashMap<>();
        output.put('A',this.A());
        output.put('B',this.B());
        output.put('C',this.C());
        output.put('D',this.D());
        output.put('E',this.E());
        output.put('F',this.F());
        output.put('G',this.G());
        output.put('H',this.H());
        output.put('I',this.I());
        output.put('J',this.J());
        output.put('K',this.K());
        output.put('L',this.L());
        output.put('M',this.M());
        output.put('N',this.N());
        output.put('O',this.O());
        output.put('P',this.P());
        output.put('Q',this.Q());
        output.put('R',this.R());
        output.put('S',this.S());
        output.put('T',this.T());
        output.put('U',this.U());
        output.put('V',this.V());
        output.put('W',this.W());
        output.put('X',this.X());
        output.put('Y',this.Y());
        output.put('Z',this.Z());
        output.put(' ',this.WHITESPACE());
        output.put('.',this.PERIOD());
        output.put('-',this.DASH());
        output.put('!',this.EXCLAMATION());
        output.put('?',this.QUESTION());
        output.put('_',this.UNDERSCORE());
        return output;
    };
}
