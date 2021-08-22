(function () {

    document.getElementById("loginButton").addEventListener('click', (e) => {
        e.preventDefault();
        let form = e.target.closest("form");
        if (form.checkValidity()) {
            makePostCallWithForm('CheckLogin', e.target.closest("form"),
                (response) => {
                    sessionStorage.setItem('username', response);
                    window.location.href = "Home.html";
                }, () => document.getElementById("errorMessage").textContent = "Enter valid credentials");
        } else {
            form.reportValidity();
        }
    });
})();