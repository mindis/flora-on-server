var isFormSubmitting = false;
/**
	Forms must have a data-path attribute
*/
function formPoster(ev, callback) {
	ev.preventDefault();
	if(ev.target.getAttribute('data-confirm')) {
	    if(!confirm('Are you sure? There\'s no way back.')) return;
	}
	isFormSubmitting = true;
	var loader = document.getElementById('loader');
	if(loader) {
	    loader.style.display = 'block';
	}

	postAJAXForm(ev.target.getAttribute('data-path'), ev.target, function(rt) {
	    var loader = document.getElementById('loader');
//	    console.log(rt);
		var rt1=JSON.parse(rt);

		if(callback) {
		    callback(rt1, ev);
		    isFormSubmitting = false;
		    if(loader) loader.style.display = 'none';
		    return;
		}

		if(rt1.success) {
		    if(rt1.msg && rt1.msg.alert)
		        alert(rt1.msg.text);

		    if(ev.target.getAttribute('data-callback') == null) {
		        if(ev.target.getAttribute('data-refresh') == 'false') {
                    alert('Ok');
                    isFormSubmitting = false;
		        } else {
                    window.location.reload();
                    return;
                }
			} else {
			    window.location = ev.target.getAttribute('data-callback');
			    return;
            }
		} else
			alert(rt1.msg);
        isFormSubmitting = false;

        if(loader) loader.style.display = 'none';
	});
}

function attachFormPosters(callback) {
	var forms=document.querySelectorAll('form.poster');
	for(var i=0;i<forms.length;i++) {
	    if(callback) {
	        addEvent('submit', forms[i], function(ev) {
	            formPoster.call(this, ev, callback);
	        });
	    } else addEvent('submit', forms[i], formPoster);
	}
}
