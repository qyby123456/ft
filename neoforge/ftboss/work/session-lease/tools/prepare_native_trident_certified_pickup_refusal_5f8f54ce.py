import hashlib
from pathlib import Path
work=Path(__file__).resolve().parents[1]
origin=work/'tools/prepare_native_trident_pickup_refusal_e07f785e.py'
assert hashlib.sha256(origin.read_bytes()).hexdigest()=='09c2b65fea86f9cad36b11b595256eb4a8ba56702cd17dc74e636110238f904a'
text=origin.read_text(encoding='utf-8').replace('e07f785e','5f8f54ce')
anchor='Object paidPickup = ((net.farmingtales.ftboss.item.NativePaidWeaponCarrier)physical).ftboss$originalOwnerPaidWeapon();'
assert text.count(anchor)==1
text=text.replace(anchor,'Object paidPickup = read(((net.farmingtales.ftboss.item.NativePaidWeaponCarrier)physical).ftboss$originalOwnerPaidWeapon(), "receipt");')
text=text.replace('paths=[Path(__file__),helper,', 'paths=[Path(__file__),origin,helper,')
namespace={'__file__':__file__,'origin':origin}
exec(compile(text,str(origin),'exec'),namespace)
