
// Select all links with hashes
$('a[href*="#"]')
// Remove links that don't actually link to anything
    .not('[href="#"]')
    .not('[href="#0"]')
    .click(function(event) {
        // On-page links
        if (
            location.pathname.replace(/^\//, '') == this.pathname.replace(/^\//, '')
            &&
            location.hostname == this.hostname
        ) {
            // Figure out element to scroll to
            var target = $(this.hash);
            target = target.length ? target : $('[name=' + this.hash.slice(1) + ']');
            // Does a scroll target exist?
            if (target.length) {
                // Only prevent default if animation is actually gonna happen
                event.preventDefault();
                $('html, body').animate({
                    scrollTop: target.offset().top -100
                }, 1000, function() {
                });
            }
        }
    });

var $contactForm = $("#contactform");
$contactForm.submit(function(a) {
    var e = $("#quote-name"),
        t = $("#quote-email"),
        r = $("#quote-textarea");
    if (a.preventDefault(), "" == e.val() || "" == t.val() || "" == r.val()) return $("#contactform:not(:has(#fillit))").append('<div id="fillit" class="alert alert-danger center-block">Please fill in all form fields.</div>'), !1;
    $contactForm.find(".alert-danger").fadeOut(), $.ajax({
        url: "//formspree.io/richardmszrs@gmail.com",
        method: "POST",
        data: $(this).serialize(),
        dataType: "json",
        beforeSend: function() {
            $contactForm.append('<div class="alert alert-info">Loading…</div>')
        },
        success: function(a) {
            $("#quote-name").val(""), $("#quote-email").val(""), $("#quote-textarea").val(""), $contactForm.find(".alert-info").hide(), $contactForm.find(".alert-success").hide(), $contactForm.append('<div class="alert alert-success center-block">Message sent!</div>'), $contactForm.find(".alert-danger").fadeOut()
        },
        error: function(a) {
            $("#quote-email").val(""), $contactForm.find(".alert-info").hide(), $contactForm.find(".alert-danger").hide(), $contactForm.append('<div class="alert alert-danger center-block">Ops, there was an error.</div>'), $contactForm.find(".alert-success").fadeOut()
        }
    })
});

$('#TableOfContents ul li ul').unwrap();$('#TableOfContents ul ul').unwrap();$('#TableOfContents ul').addClass('allul');

$(function(){
    $('.sticky').affix();
});