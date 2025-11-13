require 'json'
pkg = JSON.parse(File.read("package.json"))

Pod::Spec.new do |s|
  s.author           = pkg["author"]
  s.homepage         = pkg["homepage"]
  s.license          = pkg["license"]
  s.name             = pkg["name"]
  s.platform         = :ios, "11.0"
  s.requires_arc     = true
  s.static_framework = true

  # REMOTE
  s.source           =  { :git => pkg["repository"]["url"], :tag => pkg["version"] }
  s.source_files     = 'ios/**/*.{h,m,mm,swift}'

  # LOCAL
  #s.source           =  { :git => '../../' }
  #s.source_files     = 'ios/**/*.{h,m,mm,swift}'

  s.summary          = pkg["description"]
  s.version          = pkg["version"]
  s.dependency 'React'
  s.dependency 'eMMa', '~> 4.15.6'
end
