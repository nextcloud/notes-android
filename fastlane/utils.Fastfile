BUILD_NUMBER_RC_START = 51
BUILD_NUMBER_FINAL_START = 90

MAJOR_MULTIPLIER = 10000000
MINOR_MULTIPLIER = 10000
PATCH_MULTIPLIER = 100

platform :android do
    desc "Print version info"
    lane :printVersionInfo do
        versionInfo = getVersionInfo()
        versionComponents = parseVersionCode(versionInfo)
        print "Version code: #{versionInfo["versionCode"]}\n"
        print "Version name: #{versionInfo["versionName"]}\n"
        print "Major: #{versionComponents["major"]}\n"
        print "Minor: #{versionComponents["minor"]}\n"
        print "Patch: #{versionComponents["patch"]}\n"
        print "Build: #{versionComponents["build"]}\n"
    end

    # Usage: fastlane incrementVersion [type:major|minor|patch|rc|final]
    # For major, minor, and patch: will increment that version number by 1 and set the smaller ones to 0
    # For rc, final: will set build number to first rc/first final or increment it by 1
    desc "Increment version code and version name"
    lane :incrementVersion do |options|
        versionInfo = getVersionInfo()
        versionComponents = parseVersionCode(versionInfo)
        newVersionComponents = incrementVersionComponents(versionComponents: versionComponents, type: options[:type])
        versionNameGenerated = generateVersionName(newVersionComponents)
        versionCodeGenerated = generateVersionCode(newVersionComponents)

        print "Version code: #{versionInfo["versionCode"]} -> #{versionCodeGenerated}\n"
        print "Version name: #{versionInfo["versionName"]} -> #{versionNameGenerated}\n"
        promptYesNo()
        writeVersions(versionCode: versionCodeGenerated, versionName: versionNameGenerated)
    end


    desc "Parse major, minor, patch and build from versionCode"
    private_lane :parseVersionCode do |versionInfo|
        versionCode = versionInfo["versionCode"]
        build = versionCode % 100
        patch = (versionCode / PATCH_MULTIPLIER) % 100
        minor = (versionCode / MINOR_MULTIPLIER) % 100
        major = (versionCode / MAJOR_MULTIPLIER) % 100

        { "major" => major, "minor" => minor, "patch" => patch, "build" => build }
    end

    desc "Generate versionCode from version components"
    private_lane :generateVersionCode do |versionComponents|
        print "Generating version code from #{versionComponents}\n"
        major = versionComponents["major"]
        minor = versionComponents["minor"]
        patch = versionComponents["patch"]
        build = versionComponents["build"]
        test = major * MAJOR_MULTIPLIER + minor * MINOR_MULTIPLIER + patch * PATCH_MULTIPLIER + build
        test
    end

    desc "Compute version name from version code"
    private_lane :generateVersionName do |versionComponents|
        suffix = ""
        buildNumber = versionComponents["build"]
        case
          when buildNumber >= BUILD_NUMBER_RC_START && buildNumber < BUILD_NUMBER_FINAL_START
            rcNumber = (buildNumber - BUILD_NUMBER_RC_START) + 1
            suffix = " RC #{rcNumber}"
          when buildNumber < BUILD_NUMBER_RC_START
            suffix = " Alpha #{buildNumber + 1}"
        end
        "#{versionComponents["major"]}.#{versionComponents["minor"]}.#{versionComponents["patch"]}#{suffix}"
    end

    desc "Read versions from gradle file"
    private_lane :getVersionInfo do
        File.open("../app/build.gradle","r") do |file|
            text = file.read
            versionName = text.match(/versionName "(.*)"$/)[1]
            versionCode = text.match(/versionCode ([0-9]*)$/)[1].to_i

            { "versionCode" => versionCode, "versionName" => versionName }
        end
    end

    desc "Write versions to gradle file"
    private_lane :writeVersions do |options|
        File.open("../app/build.gradle","r+") do |file|
            text = file.read
            text.gsub!(/versionName "(.*)"$/, "versionName \"#{options[:versionName]}\"")
            text.gsub!(/versionCode ([0-9]*)$/, "versionCode #{options[:versionCode]}")
            file.rewind
            file.write(text)
            file.truncate(file.pos)
        end
    end

    private_lane :incrementVersionComponents do |options|
        versionComponents = options[:versionComponents]
        case options[:type]
        when "major"
            versionComponents["major"] = versionComponents["major"] + 1
            versionComponents["minor"] = 0
            versionComponents["patch"] = 0
            versionComponents["build"] = 0
        when "minor"
            versionComponents["minor"] = versionComponents["minor"] + 1
            versionComponents["patch"] = 0
            versionComponents["build"] = 0
        when "patch"
            versionComponents["patch"] = versionComponents["patch"] + 1
            versionComponents["build"] = 0
        when "rc"
            if versionComponents["build"] < BUILD_NUMBER_RC_START || versionComponents["build"] >= BUILD_NUMBER_FINAL_START
                versionComponents["build"] = BUILD_NUMBER_RC_START
            else
                versionComponents["build"] = versionComponents["build"] + 1
            end
        when "final"
            if versionComponents["build"] < BUILD_NUMBER_FINAL_START
                versionComponents["build"] = BUILD_NUMBER_FINAL_START
            else
                versionComponents["build"] = versionComponents["build"] + 1
            end
        else
            print "Unknown or missing version type: #{options[:type]}\n"
            exit
        end
        versionComponents
    end

    private_lane :promptYesNo do
        answer = prompt(text: "is this okay?", boolean: true)
        if !answer
            exit
        end
    end
end
