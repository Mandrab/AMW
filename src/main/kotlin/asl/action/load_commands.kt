package asl.action

import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.*
import common.translation.LiteralBuilder
import common.translation.LiteralParser.split
import org.yaml.snakeyaml.Yaml
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function
import java.util.function.Supplier
import java.util.regex.Pattern
import java.util.stream.Collectors

class load_commands : DefaultInternalAction() {

    @Throws(IOException::class)
    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<Term>): Any {
        val folder = File(File(".").canonicalPath + File.separator
                + "src" + File.separator
                + "asl_agents" + File.separator
                + "commands" + File.separator)
        val result: ListTerm = ListTermImpl()
        result.addAll(Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .filter { file: File -> file.name.startsWith("cid") }
                .collect(Collectors.groupingBy { file: File -> file.name.substring(3, file.name.indexOf("-")) })
                .values.stream().map { versions: MutableList<File> ->
                getCommand(
                    versions
                )
            }.collect(Collectors.toList()))
        un.unifies(result, args[0])
        return true
    }

    companion object {
        fun getCommand(versions: MutableList<File>): Literal {
            val infoFile = versions.stream().filter { f: File -> f.name.contains("info") }.findAny()
                    .orElseThrow { IllegalArgumentException("No info file!") }
            versions.remove(infoFile)
            return LiteralBuilder("command").setValues(
                    LiteralBuilder("id").setValues(StringTermImpl(infoFile.name.substring(0, infoFile.name.indexOf("-")))).build(),
                    LiteralBuilder("name").setValues(StringTermImpl(
                        getName(
                            infoFile
                        )
                    )).build(),
                    LiteralBuilder("description").setValues(StringTermImpl(
                        getDescription(
                            infoFile
                        )
                    )).build())
                    .setQueue(*versions.filter { f: File -> !f.name.contains("info") }
                            .mapNotNull { file: File ->
                                getVariant(
                                    file
                                )
                            }.toTypedArray()).build()
        }

        fun getName(file: File): String? {
            return openYaml(file)?.get("name") as String?
        }

        fun getDescription(file: File): String? {
            return openYaml(file)?.get("description") as String?
        }

        fun openYaml(file: File): Map<String, Any>? {
            try {
                val yaml = Yaml()
                val inputStream: InputStream = FileInputStream(file)
                return yaml.load(inputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        fun getVariant(file: File): Literal? {
            try {
                val fileName = file.name
                val terms = getRequirements(file)
                return LiteralBuilder("variant").setValues(LiteralBuilder("v_id").setValues(StringTermImpl(
                        fileName.substring(fileName.indexOf("-") + 1, fileName.indexOf(".asl")))).build(),
                        LiteralBuilder("requirements").setQueue(*terms.toTypedArray())
                                .build(), LiteralBuilder("script").setValues(StringTermImpl(
                        getScript(
                            file
                        )
                    )).build()).build()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        @Throws(IOException::class)
        fun getRequirements(file: File): List<String> {
            val lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)
            return getRequirements(
                java.lang.String.join(
                    "\n",
                    lines
                )
            )
        }

        fun getRequirements(s: String): List<String> {
            val lines: MutableList<String> = LinkedList(Arrays.asList(*s.split("\n").toTypedArray()))
            var requirements: StringBuilder = StringBuilder(lines.removeAt(0))
            while (!requirements.toString().contains("[") && !requirements.toString().contains("]")) {
                val line: String = lines.removeAt(0)
                if (!line.contains("]")) requirements.append(line) else requirements.append(line, 0, line.indexOf("]") + 1)
            }
            requirements = StringBuilder(requirements.toString().replace(" ".toRegex(), ""))
            return split(requirements.substring(requirements.indexOf("["), requirements.indexOf("]") + 1))
        }

        @Throws(IOException::class)
        fun getScript(path: File): String {
            val lines = Files.readAllLines(path.toPath(), StandardCharsets.UTF_8).stream()
                    .filter { s: String -> valid(s) }.map { s: String ->
                    removeComments(
                        s
                    )
                }.collect(Collectors.toList())
            return getScript(java.lang.String.join("\n", lines))
        }

        fun getScript(string: String): String {
            val lines: MutableList<String> = LinkedList(Arrays.asList(*string.split("\n").toTypedArray()))
            var requirementsEnded = false
            while (!requirementsEnded) {
                val line = lines[0]
                if (line.contains("]")) {
                    requirementsEnded = true
                    if (line.indexOf("]") < line.length) lines.add(0, line.substring(lines.removeAt(0).indexOf("]") + 1))
                } else {
                    lines.removeAt(0)
                }
            }
            val r = Pattern.compile("\\.[\t| ]")
            var i = 0
            while (i < lines.size) {
                val m = r.matcher(lines[i])
                if (m.find()) {
                    val l: String = lines.removeAt(i)
                    lines.add(i++, l.substring(0, l.indexOf(". ") + 1))
                    val plans = m.replaceAll("\\.\n").split("\n").toTypedArray()
                    for (j in plans.size - 1 downTo 1) {
                        lines.add(i, plans[j])
                    }
                }
                i++
            }
            val ai = AtomicInteger()
            val labelIdx = AtomicInteger()
            return "[" + lines.stream().collect(Collectors
                    .groupingBy(Function { s: String -> if (s.endsWith(".")) ai.getAndIncrement() else ai.get() })) // group string in sublist till find a plan end
                    .values.map { plan: List<String>? -> java.lang.String.join("", plan) } // join plans
                    .map { s: String -> "{" + s.substring(0, s.length - 1) + "}" }
                    .map { s: String ->
                        labelize.labelizePlan(
                            s,
                            Supplier { labelIdx.getAndIncrement().toString() + "" })
                    }
                    .joinToString(",") { s: String -> s.replace("\"", "'") } + "]"
        }

        private fun valid(s1: String): Boolean {
            val s = s1.replace(" ", "").replace("\t", "")
            return !(s.isEmpty() || s.startsWith("//"))
        }

        private fun removeComments(s: String): String {
            if (!valid(s)) return ""
            if (!s.contains("//")) return s
            val idx = s.indexOf("//")
            return s.substring(0, idx)
        }
    }
}