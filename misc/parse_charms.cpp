#include <algorithm>
#include <cassert>
#include <fstream>
#include <iostream>
#include <ostream>
#include <regex>
#include <string>

constexpr void trim(std::string& s)
{
    s.erase(s.begin(), std::find_if(s.begin(), s.end(), [](auto ch) { return !std::isspace(ch); }));
    s.erase(std::find_if(s.rbegin(), s.rend(), [](auto ch) { return !std::isspace(ch); }).base(), s.end());
}

constexpr void str_remove(std::string& s, std::string_view tok) { s.replace(s.find(tok), tok.size(), ""); }

constexpr std::string conv_case(std::string_view str)
{
    std::string result;
    bool flag = true; // start with true to handle the first character

    for (char c : str)
    {
        if (std::isupper(c))
        {
            if (!flag)
            {
                result.push_back('_');
            }
            result.push_back(std::toupper(c));
            flag = true;
        }
        else if (std::islower(c))
        {
            result.push_back(std::toupper(c));
            flag = false;
        }
        else if (std::isdigit(c))
        {
            result.push_back(c);
            flag = false;
        }
        else
        {
            flag = false;
        }
    }

    return result;
}

constexpr std::string titlecase_to_title(std::string_view str)
{
    std::string result;

    for (size_t i = 0; i < str.size(); ++i)
    {
        char c = str[i];

        if (i > 0 && std::isupper(c) && std::islower(str[i - 1]))
        {
            result.push_back(' '); // Insert space before uppercase character
        }

        result.push_back(c);
    }

    return result;
}

#define s_assert(str, pred)                                                                                                                          \
    do                                                                                                                                               \
    {                                                                                                                                                \
        if (!(pred))                                                                                                                                 \
        {                                                                                                                                            \
            std::println(std::cerr, "string assert fail \"{}\": {}", (str), #pred);                                                                  \
        }                                                                                                                                            \
    } while (0)

int main()
{
    std::ifstream ifs("CharmEffects.java.in");
    std::string buf;

    // eat until enum
    while (std::getline(ifs, buf) && !buf.contains("enum"))
        ;

    while (std::getline(ifs, buf))
    {
        trim(buf); // cleanup
        if (buf.starts_with("//") || buf.empty())
        {
            std::cout << buf << "\n";
            continue;
        }
        if (buf.contains("public"))
            break;

        // we can be fairly confident that this is a data line.
        // example:
        // BOTTLED_SUNLIGHT_COOLDOWN(BottledSunlight.CHARM_COOLDOWN, BottledSunlight.INFO, false, true, 3.0, -30.0, new double[] {-5.0, -7.5, -10.0,
        // -12.5, -15.0}),

        if (buf.contains("new double[] {"))
            str_remove(buf, "new double[] {");
        else if (buf.contains("new double [] {"))
            str_remove(buf, "new double [] {");
        else
            s_assert(buf, false);

        s_assert(buf, buf.contains("}"));
        str_remove(buf, "}");

        const auto paren = buf.find('(');
        const auto first_comma = buf.find(',');
        const auto second_comma = buf.find(',', first_comma + 1);
        s_assert(paren, paren != std::string::npos && first_comma != std::string::npos && second_comma != std::string::npos);

        std::string e_name = buf.substr(0, paren);
        std::string name = buf.substr(paren + 1, first_comma - paren - 1);
        std::string abil_name = buf.substr(first_comma + 1, second_comma - first_comma - 1);

        trim(e_name);
        trim(name);
        trim(abil_name);

        s_assert(abil_name, abil_name.ends_with(".INFO"));
        abil_name = abil_name.substr(0, abil_name.size() - 5);

        if (abil_name.starts_with("Depths"))
        {
            abil_name = abil_name.substr(6);
        }
        const auto title = titlecase_to_title(abil_name);

        // casework
        if (name.contains("CHARM_COOLDOWN"))
        {
            name = "Cooldown";
        }
        else
        {
            s_assert(name, name.starts_with('"') && name.ends_with('"'));
            name = name.substr(1, name.size() - 2);
            s_assert(name, !(name.starts_with('"') && name.ends_with('"')));
            s_assert(name + "|" + title, name.starts_with(title));
            name = name.substr(title.size() + 1);
        }

        std::println(std::cout, "{}(\"{}\", {}{}", e_name, name, conv_case(abil_name), buf.substr(second_comma));
    }
}
