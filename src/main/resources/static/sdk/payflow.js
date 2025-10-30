(function(){
  function el(tag, attrs, children){
    const e = document.createElement(tag);
    if (attrs) Object.keys(attrs).forEach(k => {
      if (k === 'class') e.className = attrs[k];
      else if (k === 'text') e.textContent = attrs[k];
      else e.setAttribute(k, attrs[k]);
    });
    (children||[]).forEach(c => e.appendChild(c));
    return e;
  }

  function buildModal(){
    const overlay = el('div', {class:'pf-modal-overlay'});
    const modal = el('div', {class:'pf-modal'});
    const header = el('div', {class:'pf-header'}, [
      el('h3', {text:'Pay with PayFlow'}),
      el('span', {class:'pf-close', title:'Close', role:'button', tabindex:'0', text:'✕'})
    ]);
    const body = el('div', {class:'pf-body'});
    modal.appendChild(header);
    modal.appendChild(body);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    return {overlay, body, closeBtn: header.lastChild};
  }

  function open(opts){
    const {overlay, body, closeBtn} = buildModal();
    const state = { busy:false };
    const errorDiv = el('div', {class:'pf-error'});

    function input(label, attrs){
      const i = el('input', Object.assign({class:'pf-input', placeholder:label}, attrs||{}));
      return i;
    }

    const amount = input('Amount', {type:'number', step:'0.01', value: (opts.amount||0).toFixed ? opts.amount.toFixed(2) : (opts.amount||0), readOnly:true});
    const email = input('Email', {type:'email'});
    const name = input('Name', {});
    const number = input('Card Number', {inputmode:'numeric', maxlength:'19'});
    const expiry = input('MM/YY', {inputmode:'numeric', maxlength:'5'});
    const cvv = input('CVV', {inputmode:'numeric', maxlength:'3'});
    const country = input('Country', {});
    const postcode = input('Postcode', {});
    const payBtn = el('button', {class:'pf-btn', text: 'Pay ' + (opts.currency||'USD') + ' ' + (opts.amount||0)});

    const row1 = el('div', {class:'pf-row'}, [expiry, cvv]);

    body.appendChild(errorDiv);
    body.appendChild(amount);
    body.appendChild(email);
    body.appendChild(name);
    body.appendChild(number);
    body.appendChild(row1);
    body.appendChild(country);
    body.appendChild(postcode);
    body.appendChild(el('div', {class:'pf-muted', text:'Use test card 4242 4242 4242 4242'}));
    body.appendChild(payBtn);

    function close(){ overlay.style.display = 'none'; document.body.removeChild(overlay); }
    overlay.style.display='flex';
    overlay.addEventListener('click', (e)=>{ if(e.target===overlay) close(); });
    closeBtn.addEventListener('click', close);

    payBtn.addEventListener('click', async function(){
      if (state.busy) return;
      errorDiv.textContent = '';
      const payload = {
        amount: parseFloat(amount.value),
        currency: opts.currency||'USD',
        customerEmail: email.value,
        customerName: name.value,
        cardNumber: number.value.replace(/\D+/g,''),
        expiry: expiry.value,
        cvv: cvv.value,
        country: country.value,
        postcode: postcode.value
      };
      if (!opts.apiKey) { errorDiv.textContent = 'Missing API key'; return; }
      state.busy = true; payBtn.disabled = true; payBtn.textContent = 'Processing...';
      try {
        const res = await fetch('/api/payments', {
          method:'POST',
          headers:{'Content-Type':'application/json','X-API-KEY': opts.apiKey},
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Request failed');
        if (typeof opts.onResult === 'function') opts.onResult(data);
        if (data.status === 'SUCCESS') close();
        else errorDiv.textContent = data.message || 'Payment failed';
      } catch (e){
        errorDiv.textContent = e.message;
      } finally {
        state.busy = false; payBtn.disabled = false; payBtn.textContent = 'Pay ' + (opts.currency||'USD') + ' ' + (opts.amount||0);
      }
    });
  }

  window.PayFlow = {
    open
  };
})();
