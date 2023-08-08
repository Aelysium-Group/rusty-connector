package group.aelysium.rustyconnector.core.lib.lang_messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static group.aelysium.rustyconnector.core.lib.lang_messaging.Lang.newlines;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public interface ASCIIAlphabet {
    int font_size = 6;

    Lang.Message WHITESPACE = () -> join(
            JoinConfiguration.noSeparators(),
            text("     "),
            text("     "),
            text("     "),
            text("     "),
            text("     "),
            text("     ")
    );

    Lang.Message A = () -> join(
            JoinConfiguration.noSeparators(),
            text(" █████╗ "),
            text("██╔══██╗"),
            text("███████║"),
            text("██╔══██║"),
            text("██║  ██║"),
            text("╚═╝  ╚═╝")
    );
    Lang.Message B = () -> join(
            JoinConfiguration.noSeparators(),
            text("██████╗ "),
            text("██╔══██╗"),
            text("██████╔╝"),
            text("██╔══██╗"),
            text("██████╔╝"),
            text("╚═════╝ ")
    );
    Lang.Message C = () -> join(
            JoinConfiguration.noSeparators(),
            text(" ██████╗"),
            text("██╔════╝"),
            text("██║     "),
            text("██║     "),
            text("╚██████╗"),
            text(" ╚═════╝")
    );
    Lang.Message D = () -> join(
            JoinConfiguration.noSeparators(),
            text("██████╗ "),
            text("██╔══██╗"),
            text("██║  ██║"),
            text("██║  ██║"),
            text("██████╔╝"),
            text("╚═════╝ ")
    );
    Lang.Message E = () -> join(
            JoinConfiguration.noSeparators(),
            text("███████╗"),
            text("██╔════╝"),
            text("█████╗  "),
            text("██╔══╝  "),
            text("███████╗"),
            text("╚══════╝")
    );
    Lang.Message F = () -> join(
            JoinConfiguration.noSeparators(),
            text("███████╗"),
            text("██╔════╝"),
            text("█████╗  "),
            text("██╔══╝  "),
            text("██║     "),
            text("╚═╝     ")
    );
    Lang.Message G = () -> join(
            JoinConfiguration.noSeparators(),
            text(" ██████╗ "),
            text("██╔════╝ "),
            text("██║  ███╗"),
            text("██║   ██║"),
            text("╚██████╔╝"),
            text(" ╚═════╝ ")
    );
    Lang.Message H = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗  ██╗"),
            text("██║  ██║"),
            text("███████║"),
            text("██╔══██║"),
            text("██║  ██║"),
            text("╚═╝  ╚═╝")
    );
    Lang.Message I = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗"),
            text("██║"),
            text("██║"),
            text("██║"),
            text("██║"),
            text("╚═╝")
    );
    Lang.Message J = () -> join(
            JoinConfiguration.noSeparators(),
            text("     ██╗"),
            text("     ██║"),
            text("     ██║"),
            text("██   ██║"),
            text("╚█████╔╝"),
            text(" ╚════╝ ")
    );
    Lang.Message K = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗  ██╗"),
            text("██║ ██╔╝"),
            text("█████╔╝ "),
            text("██╔═██╗ "),
            text("██║  ██╗"),
            text("╚═╝  ╚═╝")
    );
    Lang.Message L = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗     "),
            text("██║     "),
            text("██║     "),
            text("██║     "),
            text("███████╗"),
            text("╚══════╝")
    );
    Lang.Message M = () -> join(
            JoinConfiguration.noSeparators(),
            text("███╗   ███╗"),
            text("████╗ ████║"),
            text("██╔████╔██║"),
            text("██║╚██╔╝██║"),
            text("██║ ╚═╝ ██║"),
            text("╚═╝     ╚═╝")
    );
    Lang.Message N = () -> join(
            JoinConfiguration.noSeparators(),
            text("███╗   ██╗"),
            text("████╗  ██║"),
            text("██╔██╗ ██║"),
            text("██║╚██╗██║"),
            text("██║ ╚████║"),
            text("╚═╝  ╚═══╝")
    );
    Lang.Message O = () -> join(
            JoinConfiguration.noSeparators(),
            text(" ██████╗ "),
            text("██╔═══██╗"),
            text("██║   ██║"),
            text("██║   ██║"),
            text("╚██████╔╝"),
            text(" ╚═════╝ ")
    );
    Lang.Message P = () -> join(
            JoinConfiguration.noSeparators(),
            text("██████╗ "),
            text("██╔══██╗"),
            text("██████╔╝"),
            text("██╔═══╝ "),
            text("██║     "),
            text("╚═╝     ")
    );
    Lang.Message Q = () -> join(
            JoinConfiguration.noSeparators(),
            text(" ██████╗ "),
            text("██╔═══██╗"),
            text("██║   ██║"),
            text("██║▄▄ ██║"),
            text("╚██████╔╝"),
            text(" ╚══▀▀═╝ ")
    );
    Lang.Message R = () -> join(
            JoinConfiguration.noSeparators(),
            text("██████╗ "),
            text("██╔══██╗"),
            text("██████╔╝"),
            text("██╔══██╗"),
            text("██║  ██║"),
            text("╚═╝  ╚═╝")
    );
    Lang.Message S = () -> join(
            JoinConfiguration.noSeparators(),
            text("███████╗"),
            text("██╔════╝"),
            text("███████╗"),
            text("╚════██║"),
            text("███████║"),
            text("╚══════╝")
    );
    Lang.Message T = () -> join(
            JoinConfiguration.noSeparators(),
            text("████████╗"),
            text("╚══██╔══╝"),
            text("   ██║   "),
            text("   ██║   "),
            text("   ██║   "),
            text("   ╚═╝   ")
    );
    Lang.Message U = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗   ██╗"),
            text("██║   ██║"),
            text("██║   ██║"),
            text("██║   ██║"),
            text("╚██████╔╝"),
            text(" ╚═════╝ ")
    );
    Lang.Message V = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗   ██╗"),
            text("██║   ██║"),
            text("██║   ██║"),
            text("╚██╗ ██╔╝"),
            text(" ╚████╔╝ "),
            text("  ╚═══╝  ")
    );
    Lang.Message W = () -> join(
            JoinConfiguration.noSeparators(),
            text(" ██╗    ██╗"),
            text(" ██║    ██║"),
            text(" ██║ █╗ ██║"),
            text(" ██║███╗██║"),
            text(" ╚███╔███╔╝"),
            text("  ╚══╝╚══╝ ")
    );
    Lang.Message X = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗  ██╗"),
            text("╚██╗██╔╝"),
            text(" ╚███╔╝ "),
            text(" ██╔██╗ "),
            text("██╔╝ ██╗"),
            text("╚═╝  ╚═╝")
    );
    Lang.Message Y = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗   ██╗"),
            text("╚██╗ ██╔╝"),
            text(" ╚████╔╝ "),
            text("  ╚██╔╝  "),
            text("   ██║   "),
            text("   ╚═╝   ")
    );
    Lang.Message Z = () -> join(
            JoinConfiguration.noSeparators(),
            text("███████╗"),
            text("╚══███╔╝"),
            text("  ███╔╝ "),
            text(" ███╔╝  "),
            text("███████╗"),
            text("╚══════╝")
    );
    Lang.Message PERIOD = () -> join(
            JoinConfiguration.noSeparators(),
            text("     "),
            text("     "),
            text("     "),
            text("████╗"),
            text("████║"),
            text("╚═══╝")
    );
    Lang.Message EXCLAMATION = () -> join(
            JoinConfiguration.noSeparators(),
            text("██╗"),
            text("██║"),
            text("██║"),
            text("╚═╝"),
            text("██╗"),
            text("╚═╝")
    );
    Lang.Message QUESTION = () -> join(
            JoinConfiguration.noSeparators(),
            text("██████╗ "),
            text("╚════██╗"),
            text("  ▄███╔╝"),
            text("  ▀▀══╝ "),
            text("  ██╗   "),
            text("  ╚═╝   ")
    );
    Lang.Message DASH = () -> join(
            JoinConfiguration.noSeparators(),
            text("      "),
            text("      "),
            text("█████╗"),
            text("╚════╝"),
            text("      "),
            text("      ")
    );
    Lang.Message UNDERSCORE = () -> join(
            JoinConfiguration.noSeparators(),
            text("        "),
            text("        "),
            text("        "),
            text("        "),
            text("███████╗"),
            text("╚══════╝")
    );

    /**
     * Converts a string to an ASCIIAlphabet equivalent.
     * This method does not recognize, numbers, punctuation, nor newlines.
     * @param string
     * @return
     */
    static Component generate(String string) {
        Map<Character, Lang.Message> map = map();
        List<Component> generatedString = new ArrayList<>();

        for (int i = 0; i < font_size; i++) {
            generatedString.add(text(""));
        }

        for(char character : string.toUpperCase().toCharArray()) {
            try {
                Lang.Message asciiCharacter = map.get(character);
                for (int i = 0; i < font_size; i++) {
                    Component receivingRow = generatedString.get(i);
                    Component rowToExtract = asciiCharacter.build().children().get(i);

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
     * @param string
     * @return
     */
    static Component generate(String string, NamedTextColor color) {
        Map<Character, Lang.Message> map = map();
        List<Component> generatedString = new ArrayList<>();

        for (int i = 0; i < font_size; i++) {
            generatedString.add(text(""));
        }

        for(char character : string.toUpperCase().toCharArray()) {
            try {
                Lang.Message asciiCharacter = map.get(character);
                for (int i = 0; i < font_size; i++) {
                    Component receivingRow = generatedString.get(i);
                    Component rowToExtract = asciiCharacter.build().children().get(i);

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

    static Map<Character, Lang.Message> map() {
        Map<Character, Lang.Message> output = new HashMap<>();
        output.put('A',ASCIIAlphabet.A);
        output.put('B',ASCIIAlphabet.B);
        output.put('C',ASCIIAlphabet.C);
        output.put('D',ASCIIAlphabet.D);
        output.put('E',ASCIIAlphabet.E);
        output.put('F',ASCIIAlphabet.F);
        output.put('G',ASCIIAlphabet.G);
        output.put('H',ASCIIAlphabet.H);
        output.put('I',ASCIIAlphabet.I);
        output.put('J',ASCIIAlphabet.J);
        output.put('K',ASCIIAlphabet.K);
        output.put('L',ASCIIAlphabet.L);
        output.put('M',ASCIIAlphabet.M);
        output.put('N',ASCIIAlphabet.N);
        output.put('O',ASCIIAlphabet.O);
        output.put('P',ASCIIAlphabet.P);
        output.put('Q',ASCIIAlphabet.Q);
        output.put('R',ASCIIAlphabet.R);
        output.put('S',ASCIIAlphabet.S);
        output.put('T',ASCIIAlphabet.T);
        output.put('U',ASCIIAlphabet.U);
        output.put('V',ASCIIAlphabet.V);
        output.put('W',ASCIIAlphabet.W);
        output.put('X',ASCIIAlphabet.X);
        output.put('Y',ASCIIAlphabet.Y);
        output.put('Z',ASCIIAlphabet.Z);
        output.put(' ',ASCIIAlphabet.WHITESPACE);
        output.put('.',ASCIIAlphabet.PERIOD);
        output.put('-',ASCIIAlphabet.DASH);
        output.put('!',ASCIIAlphabet.EXCLAMATION);
        output.put('?',ASCIIAlphabet.QUESTION);
        output.put('_',ASCIIAlphabet.UNDERSCORE);
        return output;
    };
}
