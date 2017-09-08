var strength = {
    0: "Very Weak",
    1: "Weak",
    2: "So-so",
    3: "Strong",
    4: "Very Strong"
};

var password = document.getElementById('password');

if (!password) {
    password = document.getElementById('newPassword');
}

var meter = document.getElementById('password-strength-meter');
var text = document.getElementById('password-strength-text');

if (password && meter && text) {

    password.addEventListener('input', function() {
        var val = password.value;
        var result = zxcvbn(val);

        // Update the password strength meter
        meter.value = result.score + 1;

        // Update the text indicator
        if (val !== "") {
            text.innerHTML = "Password Strength: " + strength[result.score];
            var color;
            switch (result.score) {
                case 0:
                case 1: color = "red"; break;
                case 2: color = "orange"; break;
                case 3:
                case 4: color = "green"; break;
            }
            text.style.color = color;
        } else {
            text.innerHTML = "";
            meter.value = null;
        }
    });
} else {
    console.warn("Password strength indicator is not working.")
}
