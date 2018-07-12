
// const yeoman = require('yeoman-environment');
// const env = yeoman.createEnv();

// const done = () => {
//     console.log('done');
// };
// env.register(require.resolve('generator-jhipster/generators/app'));
// var context = env.run('jhipster:app', { 'skip-install': true }, done);
// var answers = {};
// context.withPrompts(answers);
// console.log('>>>> context...');
// console.log(context.withPrompts(answers));

// env.lookup(() => { 
//     env.run(
//         'jhipster', { 'skip-install': true }, 
//         (err: any) => {
//             console.log(err);
//         },
//         () => {
//             console.log('done');
//         }
//     );
// });

var runner 	= require('yeoman-gen-run');
var genName = 'jhipster';
var config = {
    "answers": {
        "appName": "my-jhipster-app",
        "useTypeScript": true,
        "useLess": true,
        "installDeps": true,
        // Added by Hadi
        //"promptValues": {
        //    "packageName": "com.mycompany.myapp",
        //    "nativeLanguage": "en"
        //},
        //"jhipsterVersion": "5.0.2",
        "applicationType": "monolith",
        "baseName": "my-jhipster-app",
        "packageName": "com.mycompany.myapp",
        "packageFolder": "com/mycompany/myapp",
        "serverPort": "8080",
        "authenticationType": "jwt",
        "cacheProvider": "ehcache",
        "enableHibernateCache": true,
        "websocket": false,
        "databaseType": "sql",
        "devDatabaseType": "h2Disk",
        "prodDatabaseType": "mysql",
        "searchEngine": false,
        "messageBroker": false,
        "serviceDiscoveryType": false,
        "buildTool": "maven",
        "enableSwaggerCodegen": false,
        "jwtSecretKey": "1e3909624e808c2ab6d1f512ffbe6a55f4726bab",
        "clientFramework": "angularX",
        "useSass": false,
        "clientPackageManager": "npm",
        //"testFrameworks": [],
        "jhiPrefix": "jhi",
        "enableTranslation": true,
        "nativeLanguage": "en",
        "languages": [
            "en"
        ]
    },
    "options": {
        "onconflict": "force",
        "npm": "true"
    },
    "cli": {
        "opts": {
            "npm": "true"
        }
    }
};
var outDir = "../my-jhipster-app";
 
runner.runGenerator(genName, config, outDir).then(function() {
    console.log('Done generating the app!');
    // runner.runGenerator(genName, { "agrs": "entity" }, outDir)
    // .then(function() {
    //     console.log('Done generating the entity!');
    // });    
});