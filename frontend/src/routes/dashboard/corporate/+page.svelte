<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import { currentUser } from '$lib/stores/auth.js';
	import { formatCurrency } from '$lib/utils/formatters.js';

	const user = $derived($currentUser);
</script>

<svelte:head>
	<title>Corporate Sponsor Dashboard | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={['CORPORATE', 'ADMIN']}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header (Cape Cobalt Surface-Dark #0A1E4A) -->
		<div class="border border-[#C8D4E8] bg-[#0A1E4A] p-8 text-white shadow-md mb-8">
			<div class="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
				<div>
					<div class="inline-block bg-[#1446A0] text-white px-2.5 py-0.5 font-mono text-[10px] font-bold uppercase tracking-widest border border-white/20 mb-2">
						CORPORATE SPONSOR &amp; ESD
					</div>
					<h1 class="font-serif text-3xl sm:text-4xl font-normal text-white">
						Welcome, {user?.name || 'Corporate Partner'}
					</h1>
					<p class="mt-2 text-sm text-[#C8D4E8] max-w-xl font-light font-sans">
						Monitor youth wage disbursements, SME capacity development, and download verified audit reports for B-BBEE scorecard compliance.
					</p>
				</div>

				<div class="flex items-center gap-3">
					<button
						type="button"
						class="bg-[#D4831A] text-white px-5 py-2.5 font-mono text-xs font-bold uppercase tracking-wider hover:bg-[#b56c0f] transition-colors"
					>
						Download B-BBEE Audit Pack
					</button>
				</div>
			</div>

			<!-- Metrics Row (Sharp) -->
			<div class="mt-8 pt-6 border-t border-[#1C3565] grid grid-cols-2 sm:grid-cols-4 gap-6">
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Youth Candidates Funded</div>
					<div class="font-mono text-2xl sm:text-3xl font-bold mt-1 text-white">45 Candidates</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Wages Disbursed</div>
					<div class="font-mono text-2xl sm:text-3xl font-bold mt-1 text-[#1A8A55]">{formatCurrency(385000)}</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">SMEs Supported</div>
					<div class="font-mono text-2xl sm:text-3xl font-bold mt-1 text-white">28 Businesses</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">B-BBEE Scorecard Points</div>
					<div class="font-mono text-2xl sm:text-3xl font-bold mt-1 text-[#D4831A]">25 / 25 Pts</div>
				</div>
			</div>
		</div>

		<!-- Impact Visuals -->
		<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
			<div class="border border-[#C8D4E8] bg-white p-6 shadow-xs">
				<h3 class="font-serif text-xl text-[#0D1B3E] mb-2">Cohort Skill Distribution</h3>
				<p class="text-xs text-[#6A84AC] font-sans mb-6">Current allocation of sponsored candidates across SME industry sectors.</p>
				<div class="space-y-4">
					<div>
						<div class="flex justify-between font-mono text-xs text-[#0D1B3E] mb-1">
							<span>Digital &amp; E-Commerce Operations</span>
							<span class="font-bold">40%</span>
						</div>
						<div class="h-2 bg-[#F8F9FC] border border-[#C8D4E8]">
							<div class="h-full bg-[#1446A0]" style="width: 40%"></div>
						</div>
					</div>
					<div>
						<div class="flex justify-between font-mono text-xs text-[#0D1B3E] mb-1">
							<span>Accounting &amp; Financial Reconciliations</span>
							<span class="font-bold">28%</span>
						</div>
						<div class="h-2 bg-[#F8F9FC] border border-[#C8D4E8]">
							<div class="h-full bg-[#1A8A55]" style="width: 28%"></div>
						</div>
					</div>
					<div>
						<div class="flex justify-between font-mono text-xs text-[#0D1B3E] mb-1">
							<span>Marketing &amp; Content Design</span>
							<span class="font-bold">22%</span>
						</div>
						<div class="h-2 bg-[#F8F9FC] border border-[#C8D4E8]">
							<div class="h-full bg-[#D4831A]" style="width: 22%"></div>
						</div>
					</div>
					<div>
						<div class="flex justify-between font-mono text-xs text-[#0D1B3E] mb-1">
							<span>Technical &amp; IT Support</span>
							<span class="font-bold">10%</span>
						</div>
						<div class="h-2 bg-[#F8F9FC] border border-[#C8D4E8]">
							<div class="h-full bg-[#7C3FE4]" style="width: 10%"></div>
						</div>
					</div>
				</div>
			</div>

			<div class="border border-[#C8D4E8] bg-white p-6 shadow-xs flex flex-col justify-between">
				<div>
					<h3 class="font-serif text-xl text-[#0D1B3E] mb-2">Verified Ledger Compliance</h3>
					<p class="text-xs text-[#6A84AC] font-sans mb-6">Every cent disbursed is backed by an immutable SME sign-off.</p>
					<div class="border border-[#1A8A55] bg-[#EDFBF3] p-4 mb-4">
						<div class="font-mono text-xs font-bold uppercase tracking-wider text-[#1A8A55] mb-1">
							[ 100% Verification Rate ]
						</div>
						<p class="text-xs text-[#0D1B3E] leading-relaxed font-sans">
							Wages are held in corporate-funded escrow and disbursed strictly upon dual digital verification by both the youth candidate and SME enterprise partner.
						</p>
					</div>
				</div>
				<div class="pt-4 border-t border-[#C8D4E8]">
					<a href="/tasks" class="font-mono text-xs font-bold uppercase tracking-wider text-[#1446A0] hover:underline">
						Inspect Platform Task Directory →
					</a>
				</div>
			</div>
		</div>
	</div>
</RoleGuard>
